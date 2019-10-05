package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.ConfigSchema;

import java.util.Collections;
import java.util.List;

@Schema
public class Person {
    public static final ConfigSchema<Person> STRUCT = ConfigSchema.of(Person.class);

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

    @Key("favourite.numbers")
    public double[] favNumber = new double[]{ 0, 1, 2, 3 };

    @Key("notes")
    @Validation(notEmptyArray = true)
    public String[] notes = new String[]{"Just a short note"};
}
