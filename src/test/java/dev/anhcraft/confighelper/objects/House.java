package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.ConfigHelper;
import dev.anhcraft.confighelper.ConfigSchema;
import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.impl.TwoWayMiddleware;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Schema
public class House implements TwoWayMiddleware {
    public static final ConfigSchema<House> STRUCT = ConfigSchema.of(House.class);

    @Key("objects")
    @IgnoreValue(ifNull = true)
    public List<Furniture> furniture = new ArrayList<>();

    @Key("family")
    @Validation(notNull = true)
    public Family family;

    @Key("street")
    public String street;

    @Override
    public Object conf2schema(ConfigSchema.Entry entry, @Nullable Object value) {
        if(entry.getKey().equals("objects")){
            ConfigurationSection cs = (ConfigurationSection) value;
            if (cs != null) {
                List<Furniture> furnitures = new ArrayList<>();
                for(String s : cs.getKeys(false)){
                    try {
                        furnitures.add(ConfigHelper.readConfig(cs.getConfigurationSection(s), Furniture.STRUCT));
                    } catch (InvalidValueException e) {
                        e.printStackTrace();
                    }
                }
                return furnitures;
            }
        }
        return value;
    }

    @Override
    public Object schema2conf(ConfigSchema.Entry entry, @Nullable Object value) {
        if(entry.getKey().equals("objects")){
            ConfigurationSection parent = new YamlConfiguration();
            int i = 0;
            for(Furniture f : (List<Furniture>) value){
                YamlConfiguration c = new YamlConfiguration();
                ConfigHelper.writeConfig(c, Furniture.STRUCT, f);
                parent.set(String.valueOf(i++), c);
            }
            return parent;
        }
        return value;
    }
}
