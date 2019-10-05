package dev.anhcraft.confighelper.schemas;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.SchemaStruct;

import java.util.Collections;
import java.util.List;

@Schema
public class PersonSchema {
    public static final SchemaStruct<PersonSchema> STRUCT = SchemaStruct.of(PersonSchema.class);

    @Key("name")
    @Explanation("Define the player's name")
    @Validation(notNull = true)
    public String name = "Steve";

    @Key("age")
    public int age = 0; // default value is 0

    @Key("jobs" )
    @Explanation({
            "The current jobs",
            "Default: worker"
    })
    @IgnoreValue(ifNull = true, ifEmptyList = true)
    public List<String> jobs = Collections.singletonList("worker");

    @Key("favourite.number")
    public double favNumber;
}
