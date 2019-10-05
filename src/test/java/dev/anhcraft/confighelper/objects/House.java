package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.ConfigSchema;

import java.util.ArrayList;
import java.util.List;

@Schema
public class House {
    public static final ConfigSchema<House> STRUCT = ConfigSchema.of(House.class);

    @Key("objects")
    @IgnoreValue(ifNull = true)
    public List<Furniture> furniture = new ArrayList<>();

    @Key("family")
    @Validation(notNull = true)
    public Family family;

    @Key("street")
    public String street;
}
