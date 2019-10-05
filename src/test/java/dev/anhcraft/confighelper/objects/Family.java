package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.ConfigSchema;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Schema
public class Family {
    public static final ConfigSchema<Family> STRUCT = ConfigSchema.of(Family.class);

    @Key("members")
    @Explanation("All members in this family")
    @IgnoreValue(ifNull = true) // the value defaults to non-null, we don't want the null will override it later
    private List<Person> members = new ArrayList<>();

    @NotNull
    public List<Person> getMembers() {
        return members;
    }
}
