package dev.anhcraft.confighelper.schemas;

import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.SchemaStruct;

import java.util.ArrayList;
import java.util.List;

@Schema
public class HouseSchema {
    public static final SchemaStruct<HouseSchema> STRUCT = SchemaStruct.of(HouseSchema.class);

    @Key("objects")
    @IgnoreValue(ifNull = true)
    public List<FurnitureSchema> furniture = new ArrayList<>();

    @Key("family")
    @Validation(notNull = true)
    public FamilySchema family;

    @Key("street")
    public String street;
}
