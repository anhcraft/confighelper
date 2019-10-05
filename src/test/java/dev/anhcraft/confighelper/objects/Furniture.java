package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.ConfigSchema;
import org.bukkit.Location;
import org.bukkit.Material;

@Schema
public class Furniture {
    public static final ConfigSchema<Furniture> STRUCT = ConfigSchema.of(Furniture.class);

    @Key("id")
    public int id;

    @Key("material")
    @IgnoreValue(ifNull = true)
    public Material material = Material.AIR;

    @Key("damaged")
    public int damaged = 0;

    @Key("position")
    public Location position;

    @Middleware(Middleware.Direction.CONFIG_TO_SCHEMA)
    private Object onHandle1(ConfigSchema.Entry entry, Object value){
        if(entry.getKey().value().equals("material")){
            return Material.getMaterial(String.valueOf(value));
        }
        return value;
    }

    @Middleware(Middleware.Direction.SCHEMA_TO_CONFIG)
    public Object onHandle2(ConfigSchema.Entry entry, Object value){
        if(entry.getKey().value().equals("material")){
            return value.toString();
        }
        return value;
    }
}
