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
    @PrettyEnum
    public Material material = Material.AIR;

    @Key("damaged")
    public int damaged = 0;

    @Key("position")
    public Location position;
}
