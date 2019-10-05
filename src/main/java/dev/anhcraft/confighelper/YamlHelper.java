package dev.anhcraft.confighelper;

import com.google.common.base.Preconditions;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Middleware;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class YamlHelper {
    public static <T> T readConfig(@NotNull ConfigurationSection bukkitConf, @NotNull SchemaStruct<T> schemaStruct) throws InvalidValueException {
        return readConfig(bukkitConf, schemaStruct, schemaStruct.newInstance());
    }

    public static <T> T readConfig(@NotNull ConfigurationSection bukkitConf, @NotNull SchemaStruct<T> schemaStruct, @Nullable T schema) throws InvalidValueException {
        Preconditions.checkNotNull(bukkitConf);
        Preconditions.checkNotNull(schemaStruct);
        for (String k : schemaStruct.listKeys()){
            SchemaStruct.Entry entry = schemaStruct.getEntry(k);
            if(entry == null) continue;
            Field field = entry.getField();

            Object value = bukkitConf.get(k);
            value = schemaStruct.callMiddleware(entry, value, schema, Middleware.Direction.CONFIG_TO_SCHEMA);

            if(entry.getSchemaStruct() != null && value instanceof ConfigurationSection){
                value = readConfig((ConfigurationSection) value, entry.getSchemaStruct());
            } else  {
                if(entry.getValidation() != null){
                    Validation validation = entry.getValidation();
                    if(value != null){
                        if(validation.notEmptyArray() && Array.getLength(value) == 0){
                            throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_ARRAY);
                        }
                        if(validation.notEmptyList() && List.class.isAssignableFrom(field.getType())){
                            if(((List) value).isEmpty()){
                                throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_COLLECTION);
                            }
                        }
                        if(validation.notEmptyString() && CharSequence.class.isAssignableFrom(field.getType())){
                            if(((CharSequence) value).length() == 0){
                                throw new InvalidValueException(k, InvalidValueException.Reason.EMPTY_STRING);
                            }
                        }
                    } else if(validation.notNull()){
                        throw new InvalidValueException(k, InvalidValueException.Reason.NULL);
                    }
                }

                if(entry.getIgnoreValue() != null){
                    IgnoreValue ignoreValue = entry.getIgnoreValue();
                    if(value != null){
                        if(ignoreValue.ifEmptyArray() && Array.getLength(value) == 0){
                            continue;
                        }
                        if(ignoreValue.ifEmptyList() && List.class.isAssignableFrom(field.getType())){
                            if(((List) value).isEmpty()){
                                continue;
                            }
                        }
                        if(ignoreValue.ifEmptyString() && CharSequence.class.isAssignableFrom(field.getType())){
                            if(((CharSequence) value).length() == 0){
                                continue;
                            }
                        }
                    } else if(ignoreValue.ifNull()){
                        continue;
                    }
                }

                if(value != null && entry.getSchemaStruct() != null && entry.getComponentClass() != null){
                    if(List.class.isAssignableFrom(field.getType())){
                        List<?> olist = (List<?>) value;
                        if(!olist.isEmpty()){
                            List<Object> nlist = new ArrayList<>();
                            for(Object o : olist){
                                if(o instanceof ConfigurationSection){
                                    nlist.add(readConfig((ConfigurationSection) o, entry.getSchemaStruct()));
                                } else {
                                    nlist.add(o);
                                }
                            }
                            value = nlist;
                        }
                    }
                    if(field.getType().isArray()){
                        int len = Array.getLength(value);
                        if(len > 0){
                            Object n = Array.newInstance(entry.getComponentClass(), len);
                            for(int i = 0; i < len; i++){
                                Object o = Array.get(value, i);
                                if(o instanceof ConfigurationSection){
                                    Array.set(n, i, readConfig((ConfigurationSection) o, entry.getSchemaStruct()));
                                } else {
                                    Array.set(n, i, o);
                                }
                            }
                            value = n;
                        }
                    }
                }
            }

            try {
                field.set(schema, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return schema;
    }

    public static <T> void writeConfig(@NotNull ConfigurationSection bukkitConf, @NotNull SchemaStruct<T> schemaStruct, @Nullable Object schema) {
        Preconditions.checkNotNull(bukkitConf);
        Preconditions.checkNotNull(schemaStruct);
        for (String k : schemaStruct.listKeys()) {
            SchemaStruct.Entry entry = schemaStruct.getEntry(k);
            if (entry == null) continue;

            Object value = schemaStruct.getValue(entry, schema);
            value = schemaStruct.callMiddleware(entry, value, schema, Middleware.Direction.SCHEMA_TO_CONFIG);

            if(entry.getSchemaStruct() != null && value != null) {
                if(value.getClass().isAnnotationPresent(Schema.class)){
                    YamlConfiguration conf = new YamlConfiguration();
                    writeConfig(conf, entry.getSchemaStruct(), value);
                    value = conf;
                } else if(entry.getComponentClass() != null){
                    if(List.class.isAssignableFrom(entry.getField().getType())){
                        List<?> olist = (List<?>) value;
                        if(!olist.isEmpty()){
                            List<ConfigurationSection> nlist = new ArrayList<>();
                            for(Object o : olist){
                                YamlConfiguration conf = new YamlConfiguration();
                                writeConfig(conf, entry.getSchemaStruct(), o);
                                nlist.add(conf);
                            }
                            value = nlist;
                        }
                    }
                    if(entry.getField().getType().isArray()){
                        int len = Array.getLength(value);
                        if(len > 0){
                            ConfigurationSection[] n = new ConfigurationSection[len];
                            for(int i = 0; i < len; i++){
                                Object o = Array.get(value, i);
                                YamlConfiguration conf = new YamlConfiguration();
                                writeConfig(conf, entry.getSchemaStruct(), o);
                                n[i] = conf;
                            }
                            value = n;
                        }
                    }
                }
            }
            bukkitConf.set(k, value);
        }
    }
}
