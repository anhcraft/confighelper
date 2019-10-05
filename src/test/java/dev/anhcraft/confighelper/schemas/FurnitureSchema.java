package dev.anhcraft.confighelper.schemas;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.SchemaStruct;
import org.bukkit.Location;
import org.bukkit.Material;

@Schema
public class FurnitureSchema {
    public static final SchemaStruct<FurnitureSchema> STRUCT = SchemaStruct.of(FurnitureSchema.class);

    @Key("id")
    @Validation(notNull = true)
    public int id;

    @Key("material")
    @IgnoreValue(ifNull = true)
    public Material material = Material.AIR;

    @Key("damaged")
    public int damaged = 0;

    @Key("position")
    @Validation(notNull = true)
    public Location position;

    @Middleware(Middleware.Direction.CONFIG_TO_SCHEMA)
    private Object onHandle1(SchemaStruct.Entry entry, Object value){
        if(entry.getKey().value().equals("material")){
            return Material.getMaterial(String.valueOf(value));
        }
        return value;
    }

    @Middleware(Middleware.Direction.SCHEMA_TO_CONFIG)
    public Object onHandle2(SchemaStruct.Entry entry, Object value){
        if(entry.getKey().value().equals("material")){
            return value.toString();
        }
        return value;
    }
}
