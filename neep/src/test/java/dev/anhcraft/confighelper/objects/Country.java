package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Schema
public class Country {
    @Key("name")
    @Explanation("The name of this country")
    @Validation(notNull = true)
    private String name;

    @Key("towns")
    @IgnoreValue(ifNull = true)
    private List<Town> towns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Town> getTowns() {
        return towns;
    }
}
