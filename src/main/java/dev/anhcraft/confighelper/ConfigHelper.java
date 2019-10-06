package dev.anhcraft.confighelper;

import com.google.common.base.Preconditions;
import com.google.gson.internal.$Gson$Preconditions;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Middleware;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.utils.EnumUtil;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.enums.EnumUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigHelper {
    @NotNull
    public static <T> T readConfig(@NotNull ConfigurationSection bukkitConf, @NotNull ConfigSchema<T> configSchema) throws InvalidValueException {
        return readConfig(bukkitConf, configSchema, configSchema.newInstance());
    }

    @Contract("_, _, null -> null")
    public static <T> T readConfig(@NotNull ConfigurationSection bukkitConf, @NotNull ConfigSchema<T> configSchema, @Nullable T object) throws InvalidValueException {
        Preconditions.checkNotNull(bukkitConf);
        Preconditions.checkNotNull(configSchema);
        for (String k : configSchema.listKeys()){
            ConfigSchema.Entry entry = configSchema.getEntry(k);
            if(entry == null) continue;
            Field field = entry.getField();

            Object value = bukkitConf.get(k);
            value = configSchema.callMiddleware(entry, value, object, Middleware.Direction.CONFIG_TO_SCHEMA);

            if(entry.getValueSchema() != null && value instanceof ConfigurationSection){
                value = readConfig((ConfigurationSection) value, entry.getValueSchema());
            } else {
                if(entry.isPrettyEnum() && value != null && String.class.isAssignableFrom(value.getClass())) {
                    try {
                        value = EnumUtil.findEnum((Class<? extends Enum>) field.getType(), (String) value);
                    } catch (IllegalArgumentException e){
                        value = null;
                    }
                }
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

                if(value != null && entry.getComponentClass() != null){
                    if(List.class.isAssignableFrom(field.getType())){
                        List<?> olist = (List<?>) value;
                        if(!olist.isEmpty()){
                            List<Object> nlist = new ArrayList<>();
                            for(Object o : olist){
                                if(entry.getValueSchema() != null && o instanceof ConfigurationSection){
                                    nlist.add(readConfig((ConfigurationSection) o, entry.getValueSchema()));
                                } else if(entry.isPrettyEnum() && o instanceof String) {
                                    nlist.add(Enum.valueOf((Class<? extends Enum>) entry.getComponentClass(), (String) o));
                                } else {
                                    nlist.add(o);
                                }
                            }
                            value = nlist;
                        }
                    }
                    else if(field.getType().isArray()){
                        int len = Array.getLength(value);
                        if(len > 0){
                            Object n = Array.newInstance(entry.getComponentClass(), len);
                            for(int i = 0; i < len; i++){
                                Object o = Array.get(value, i);
                                if(entry.getValueSchema() != null && o instanceof ConfigurationSection){
                                    Array.set(n, i, readConfig((ConfigurationSection) o, entry.getValueSchema()));
                                } else if(entry.isPrettyEnum() && o instanceof String) {
                                    Array.set(n, i, Enum.valueOf((Class<? extends Enum>) entry.getComponentClass(), (String) o));
                                } else if(!entry.getComponentClass().isPrimitive() || o != null) {
                                    Array.set(n, i, o);
                                }
                            }
                            value = n;
                        }
                    }
                }
            }
            try {
                field.set(object, value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public static <T> void writeConfig(@NotNull ConfigurationSection bukkitConf, @NotNull ConfigSchema<T> configSchema, @Nullable Object object) {
        Preconditions.checkNotNull(bukkitConf);
        Preconditions.checkNotNull(configSchema);
        for (String k : configSchema.listKeys()) {
            ConfigSchema.Entry entry = configSchema.getEntry(k);
            if (entry == null) continue;

            Object value = configSchema.getValue(entry, object);
            value = configSchema.callMiddleware(entry, value, object, Middleware.Direction.SCHEMA_TO_CONFIG);

            if(value != null) {
                if(entry.isPrettyEnum() && value.getClass().isEnum()){
                    value = value.toString();
                } else if(entry.getValueSchema() != null && value.getClass().isAnnotationPresent(Schema.class)){
                    YamlConfiguration conf = new YamlConfiguration();
                    writeConfig(conf, entry.getValueSchema(), value);
                    value = conf;
                } else if(entry.getComponentClass() != null){
                    if(List.class.isAssignableFrom(entry.getField().getType())){
                        List<?> olist = (List<?>) value;
                        if(!olist.isEmpty()){
                            if(entry.isPrettyEnum()){
                                List<Object> nlist = new ArrayList<>();
                                for(Object o : olist){
                                    nlist.add(o.toString());
                                }
                                value = nlist;
                            } else if(entry.getValueSchema() != null){
                                List<Object> nlist = new ArrayList<>();
                                for(Object o : olist){
                                    YamlConfiguration conf = new YamlConfiguration();
                                    writeConfig(conf, entry.getValueSchema(), o);
                                    nlist.add(conf);
                                }
                                value = nlist;
                            }
                        }
                    }
                    if(entry.getField().getType().isArray()){
                        int len = Array.getLength(value);
                        if(len > 0){
                            if(entry.isPrettyEnum()) {
                                Object[] n = new Object[len];
                                for (int i = 0; i < len; i++) {
                                    n[i] = Array.get(value, i).toString();
                                }
                                value = n;
                            } else if(entry.getValueSchema() != null){
                                Object[] n = new Object[len];
                                for (int i = 0; i < len; i++) {
                                    Object o = Array.get(value, i);
                                    YamlConfiguration conf = new YamlConfiguration();
                                    writeConfig(conf, entry.getValueSchema(), o);
                                    n[i] = conf;
                                }
                                value = n;
                            }
                        }
                    }
                }
            }
            bukkitConf.set(k, value);
        }
    }
}
