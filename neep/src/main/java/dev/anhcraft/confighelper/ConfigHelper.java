package dev.anhcraft.confighelper;

import com.google.common.base.Preconditions;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Middleware;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.utils.EnumUtil;
import dev.anhcraft.neep.NeepConfig;
import dev.anhcraft.neep.struct.NeepComponent;
import dev.anhcraft.neep.struct.NeepList;
import dev.anhcraft.neep.struct.NeepSection;
import dev.anhcraft.neep.struct.NeepString;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ConfigHelper {
    private static final EntryFilter DEFAULT_ENTRY_FILTER = newOptions();

    @NotNull
    public static EntryFilter newOptions(){
        return new EntryFilter() {
            @Override
            protected boolean checkSection(Object val) {
                return val instanceof NeepSection && ((NeepSection) val).getKeys().isEmpty();
            }
        };
    }

    @NotNull
    public static <T> T readConfig(@NotNull NeepConfig neepConfig, @NotNull ConfigSchema<T> configSchema) throws InvalidValueException {
        return readConfig(neepConfig, configSchema, configSchema.newInstance());
    }

    @Contract("_, _, null -> null")
    public static <T> T readConfig(@NotNull NeepConfig neepConfig, @NotNull ConfigSchema<T> configSchema, @Nullable T object) throws InvalidValueException {
        Preconditions.checkNotNull(neepConfig);
        Preconditions.checkNotNull(configSchema);

        for (String k : configSchema.listKeys()){
            ConfigSchema.Entry entry = configSchema.getEntry(k);
            if(entry == null) {
                continue;
            }
            Field field = entry.getField();

            NeepComponent component = neepConfig.getComponent(k);
            Object value = null;
            if(component != null) {
                value = component.getValueAsObject();
            }
            value = configSchema.callMiddleware(entry, value, object, Middleware.Direction.CONFIG_TO_SCHEMA);

            if(entry.getValueSchema() != null && component instanceof NeepSection){
                value = readConfig(NeepConfig.of(component.asSection()), entry.getValueSchema());
            } else {
                if(entry.isPrettyEnum() && value != null && String.class.isAssignableFrom(value.getClass())) {
                    value = EnumUtil.findEnum((Class<? extends Enum>) field.getType(), (String) value);
                }

                if(entry.getValidation() != null){
                    Validation v = entry.getValidation();
                    if(value != null){
                        if(v.notEmptyArray() && value.getClass().isArray() && Array.getLength(value) == 0){
                            throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_ARRAY);
                        }
                        if(v.notEmptyList() && List.class.isAssignableFrom(field.getType())){
                            if(((List) value).isEmpty()){
                                throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_COLLECTION);
                            }
                        }
                        if(v.notEmptyString() && CharSequence.class.isAssignableFrom(field.getType())){
                            if(((CharSequence) value).length() == 0){
                                throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_STRING);
                            }
                        }
                    } else if(v.notNull()){
                        throw new InvalidValueException(k, InvalidValueException.Reason.NULL);
                    }
                }

                if(entry.getIgnoreValue() != null){
                    IgnoreValue iv = entry.getIgnoreValue();
                    if(value != null){
                        if(iv.ifEmptyArray() && Array.getLength(value) == 0){
                            continue;
                        }
                        if(iv.ifEmptyList() && List.class.isAssignableFrom(field.getType())){
                            if(((List) value).isEmpty()){
                                continue;
                            }
                        }
                        if(iv.ifEmptyString() && CharSequence.class.isAssignableFrom(field.getType())){
                            if(((CharSequence) value).length() == 0){
                                continue;
                            }
                        }
                    } else if(iv.ifNull()){
                        continue;
                    }
                }

                if(value != null && entry.getComponentClass() != null && component != null){
                    if(List.class.isAssignableFrom(field.getType())){
                        NeepList<?> list = component.asList();
                        if(list.size() > 0){
                            List<Object> nlist = new ArrayList<>();
                            for(Object o : list){
                                if(entry.getValueSchema() != null && o instanceof NeepSection){
                                    nlist.add(readConfig(NeepConfig.of((NeepSection) o), entry.getValueSchema()));
                                } else if(entry.isPrettyEnum() && o instanceof NeepString) {
                                    nlist.add(EnumUtil.findEnum((Class<? extends Enum>) entry.getComponentClass(), ((NeepString) o).getValue()));
                                } else if(o instanceof NeepComponent) {
                                    nlist.add(((NeepComponent) o).getValueAsObject());
                                }
                            }
                            value = nlist;
                        }
                    }
                    else if(field.getType().isArray()){
                        NeepList<?> list = component.asList();
                        if(list.size() > 0){
                            Object arr = Array.newInstance(entry.getComponentClass(), list.size());
                            int i = 0;
                            for(Object o : list){
                                if(entry.getValueSchema() != null && o instanceof NeepSection){
                                    Array.set(arr, i, readConfig(NeepConfig.of((NeepSection) o), entry.getValueSchema()));
                                } else if(entry.isPrettyEnum() && o instanceof NeepString) {
                                    Array.set(arr, i, EnumUtil.findEnum((Class<? extends Enum>) entry.getComponentClass(), ((NeepString) o).getValue()));
                                } else if(o instanceof NeepComponent) {
                                    Array.set(arr, i, ((NeepComponent) o).getValueAsObject());
                                }
                                i++;
                            }
                            value = arr;
                        }
                    }
                }
            }

            if(!field.getType().isPrimitive() || value != null) {
                try {
                    field.set(object, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return object;
    }

    public static <T> void writeConfig(@NotNull NeepConfig neepConfig, @NotNull ConfigSchema<T> configSchema, @Nullable Object object) {
        writeConfig(neepConfig, configSchema, object, DEFAULT_ENTRY_FILTER);
    }

    public static <T> void writeConfig(@NotNull NeepConfig neepConfig, @NotNull ConfigSchema<T> configSchema, @Nullable Object object, @NotNull EntryFilter filter) {
        Preconditions.checkNotNull(neepConfig);
        Preconditions.checkNotNull(configSchema);
        Preconditions.checkNotNull(filter);

        for (String k : configSchema.listKeys()) {
            ConfigSchema.Entry entry = configSchema.getEntry(k);
            if (entry == null) {
                continue;
            }

            Object value = configSchema.getValue(entry, object);
            value = configSchema.callMiddleware(entry, value, object, Middleware.Direction.SCHEMA_TO_CONFIG);

            if(value != null) {
                if(entry.isPrettyEnum() && value.getClass().isEnum()){
                    value = value.toString();
                } else if(entry.getValueSchema() != null && value.getClass().isAnnotationPresent(Schema.class)){
                    NeepConfig conf = NeepConfig.create();
                    writeConfig(conf, entry.getValueSchema(), value, filter);
                    value = conf.getRoot();
                } else if(entry.getComponentClass() != null){
                    if(List.class.isAssignableFrom(value.getClass())){
                        List<?> list = (List<?>) value;
                        if(!list.isEmpty()){
                            if(entry.isPrettyEnum()){
                                List<Object> newList = new ArrayList<>();
                                for(Object o : list){
                                    newList.add(o.toString());
                                }
                                value = newList;
                            } else if(entry.getValueSchema() != null){
                                List<Object> newList = new ArrayList<>();
                                for(Object o : list){
                                    NeepConfig conf = NeepConfig.create();
                                    writeConfig(conf, entry.getValueSchema(), o, filter);
                                    newList.add(conf.getRoot());
                                }
                                value = newList;
                            }
                        }
                    }
                    if(value.getClass().isArray()){
                        int len = Array.getLength(value);
                        if(len > 0){
                            if(entry.isPrettyEnum()) {
                                Object[] arr = new Object[len];
                                for (int i = 0; i < len; i++) {
                                    arr[i] = Array.get(value, i).toString();
                                }
                                value = arr;
                            } else if(entry.getValueSchema() != null){
                                Object[] arr = new Object[len];
                                for (int i = 0; i < len; i++) {
                                    Object o = Array.get(value, i);
                                    NeepConfig conf = NeepConfig.create();
                                    writeConfig(conf, entry.getValueSchema(), o, filter);
                                    arr[i] = conf.getRoot();
                                }
                                value = arr;
                            }
                        }
                    }
                }
            }

            if(filter.check(value) && value != null) {
                neepConfig.set(k, value);
            }
        }
    }
}
