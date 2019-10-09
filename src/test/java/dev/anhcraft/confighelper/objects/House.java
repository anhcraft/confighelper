package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema
public class House {
    @Key("people")
    @IgnoreValue(ifNull = true)
    private List<Human> people = new ArrayList<>();

    public List<Human> getPeople() {
        return people;
    }
}
