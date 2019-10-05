package dev.anhcraft.confighelper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import dev.anhcraft.confighelper.annotation.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.BiConsumer;

public class ConfigSchema<T> {
    @NotNull
    public static <T> ConfigSchema<T> of(@NotNull Class<T> schemaClass){
        Preconditions.checkNotNull(schemaClass);
        Preconditions.checkArgument(schemaClass.isAnnotationPresent(Schema.class), "No @schema found");
        return fSchema(schemaClass);
    }

    private static <T> ConfigSchema<T> fSchema(Class<T> schemaClass){
        ConfigSchema<T> configSchema = new ConfigSchema<>(schemaClass);
        Field[] fields = schemaClass.getDeclaredFields();
        for(Field f : fields) {
            f.setAccessible(true);
            Key key = f.getAnnotation(Key.class);
            if(key == null) continue;
            Explanation explanation = f.getAnnotation(Explanation.class);
            IgnoreValue ignoreValue = f.getAnnotation(IgnoreValue.class);
            Validation validation = f.getAnnotation(Validation.class);
            ConfigSchema ss = null;
            Class<?> componentClass = null;

            if(f.getType().isAnnotationPresent(Schema.class)) {
                ss = fSchema(f.getType());
            } else if(f.getType().isArray() && f.getType().getComponentType().isAnnotationPresent(Schema.class)){
                componentClass = f.getType().getComponentType();
                ss = fSchema(componentClass);
            } else if(List.class.isAssignableFrom(f.getType())){
                Type type = f.getGenericType();
                if (type instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) type;
                    try {
                        Class<?> ac = Class.forName(pType.getActualTypeArguments()[0].getTypeName());
                        if(ac.isAnnotationPresent(Schema.class)){
                            componentClass = ac;
                            ss = fSchema(ac);
                        }
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }

            Entry e = new Entry(f, key, explanation, validation, ignoreValue, ss, componentClass);
            configSchema.entries.put(e.key.value(), e);
        }

        Method[] methods = schemaClass.getDeclaredMethods();
        for(Method m : methods){
            m.setAccessible(true);
            Middleware middleware = m.getAnnotation(Middleware.class);
            if(middleware != null) {
                Class<?>[] params = m.getParameterTypes();
                if(params.length >= 2 && params[0].equals(ConfigSchema.Entry.class) && params[1].equals(Object.class) && !m.getReturnType().equals(Void.class)) {
                    configSchema.middleware.put(m, middleware.value());
                }
            }
        }
        return configSchema;
    }

    private final Class<T> schemaClass;
    private final Map<String, Entry> entries = new HashMap<>();
    private final Map<Method, Middleware.Direction> middleware = new HashMap<>();
    private Constructor constructor;

    public ConfigSchema(@NotNull Class<T> schemaClass) {
        Preconditions.checkNotNull(schemaClass);
        this.schemaClass = schemaClass;
        try {
            constructor = schemaClass.getConstructor();
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    T newInstance() {
        try {
            return (T) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    @NotNull
    public Class<T> getSchemaClass() {
        return schemaClass;
    }

    @NotNull
    public Collection<String> listKeys(){
        return entries.keySet();
    }

    @Nullable
    public ConfigSchema.Entry getEntry(@Nullable String key){
        return entries.get(key);
    }

    @Nullable
    public Object getValue(@NotNull ConfigSchema.Entry entry, @Nullable Object object){
        return getValue(entry, object, null);
    }

    @Nullable
    public Object getValue(@Nullable String key, @Nullable Object object){
        return getValue(key, object, null);
    }

    @Nullable
    public Object getValue(@NotNull ConfigSchema.Entry entry, @Nullable Object object, @Nullable Object defaultValue){
        Preconditions.checkNotNull(entry);
        try {
            return entry.getField().get(object);
        } catch (IllegalAccessException e) {
            return defaultValue;
        }
    }

    @Nullable
    public Object getValue(@Nullable String key, @Nullable Object schema, @Nullable Object defaultValue){
        Entry entry = entries.get(key);
        if(entry == null) return null;
        try {
            return entry.getField().get(schema);
        } catch (IllegalAccessException e) {
            return defaultValue;
        }
    }

    @Nullable
    public Object callMiddleware(@NotNull ConfigSchema.Entry entry, @Nullable Object value, @Nullable Object object, @NotNull Middleware.Direction dir) {
        Preconditions.checkNotNull(entry);
        Preconditions.checkNotNull(dir);
        for(Map.Entry<Method, Middleware.Direction> e : middleware.entrySet()){
            if(e.getValue() != Middleware.Direction.ALL && e.getValue() != dir) continue;
            try {
                value = e.getKey().invoke(object, entry, value);
            } catch (IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
        }
        return value;
    }

    @NotNull
    public String printMarkdownTable(){
        StringBuilder builder = new StringBuilder("| Key | Type | Restriction | Explanation |\n")
                .append("| - | - | - | - |\n");
        for (Map.Entry<String, Entry> e : entries.entrySet()){
            Entry entry = e.getValue();
            String type = entry.componentClass == null ?
                    entry.field.getType().getSimpleName() :
                    entry.componentClass.getSimpleName();
            builder.append("| ").append(e.getKey()).append(" | ").append(type);
            if(entry.getValidation() == null){
                builder.append(" |");
            } else {
                Validation validation = entry.getValidation();
                StringBuilder vb = new StringBuilder();
                if(validation.notNull())
                    vb.append("**not-null** ");
                if(validation.notEmptyString() || validation.notEmptyArray() || validation.notEmptyList())
                    vb.append("**not-empty** ");
                builder.append(" | ").append(vb).append(" |");
            }
            if(entry.getExplanation() == null){
                builder.append(" | | ");
            } else {
                builder.append(" | ").append(Joiner.on(". ").join(entry.explanation.value())).append(" | ");
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @NotNull
    public String printMarkdownList(@NotNull String heading){
        Preconditions.checkNotNull(heading);
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Entry> e : entries.entrySet()){
            Entry entry = e.getValue();
            String type = entry.componentClass == null ?
                    entry.field.getType().getSimpleName() :
                    entry.componentClass.getSimpleName();
            builder.append(heading).append(e.getKey()).append(" (").append(type).append(")");
            if(entry.getExplanation() != null){
                for(String s : entry.explanation.value()){
                    builder.append("\n  - ").append(s);
                }
            }
            if(entry.getValidation() != null){
                Validation validation = entry.getValidation();
                StringBuilder vb = new StringBuilder();
                if(validation.notNull())
                    vb.append("**not-null** ");
                if(validation.notEmptyString() || validation.notEmptyArray() || validation.notEmptyList())
                    vb.append("**not-empty** ");
                builder.append("\n  - **Restriction**: ").append(vb);
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigSchema<?> schema = (ConfigSchema<?>) o;
        return schemaClass.equals(schema.schemaClass) &&
                entries.equals(schema.entries) &&
                middleware.equals(schema.middleware) &&
                constructor.equals(schema.constructor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schemaClass);
    }

    public static class Entry {
        private Field field;
        private Key key;
        private Explanation explanation;
        private Validation validation;
        private IgnoreValue ignoreValue;
        private ConfigSchema configSchema;
        private Class<?> componentClass;

        public Entry(@NotNull Field field, @NotNull Key key, @Nullable Explanation explanation, @Nullable Validation validation, @Nullable IgnoreValue ignoreValue, @Nullable ConfigSchema configSchema, @Nullable Class<?> componentClass) {
            Preconditions.checkNotNull(field);
            this.field = field;
            this.key = key;
            this.explanation = explanation;
            this.validation = validation;
            this.ignoreValue = ignoreValue;
            this.configSchema = configSchema;
            this.componentClass = componentClass;
        }

        @NotNull
        public Field getField() {
            return field;
        }

        @NotNull
        public Key getKey() {
            return key;
        }

        @Nullable
        public Explanation getExplanation() {
            return explanation;
        }

        @Nullable
        public Validation getValidation() {
            return validation;
        }

        @Nullable
        public IgnoreValue getIgnoreValue() {
            return ignoreValue;
        }

        @Nullable
        public ConfigSchema<?> getValueSchema() {
            return configSchema;
        }

        @Nullable
        public Class<?> getComponentClass() {
            return componentClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return field.equals(entry.field) &&
                    key.equals(entry.key) &&
                    Objects.equals(explanation, entry.explanation) &&
                    Objects.equals(validation, entry.validation) &&
                    Objects.equals(ignoreValue, entry.ignoreValue) &&
                    Objects.equals(configSchema, entry.configSchema) &&
                    Objects.equals(componentClass, entry.componentClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(field, key);
        }
    }
}
