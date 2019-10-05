package dev.anhcraft.confighelper.schemas;

import dev.anhcraft.confighelper.annotation.*;
import dev.anhcraft.confighelper.SchemaStruct;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Schema
public class FamilySchema {
    public static final SchemaStruct<FamilySchema> STRUCT = SchemaStruct.of(FamilySchema.class);

    @Key("members")
    @Explanation("All members in this family")
    @IgnoreValue(ifNull = true) // the value defaults to non-null, we don't want the null will override it later
    private List<PersonSchema> members = new ArrayList<>();

    @NotNull
    public List<PersonSchema> getMembers() {
        return members;
    }
}
