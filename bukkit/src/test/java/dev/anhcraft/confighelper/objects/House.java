package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.*;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

@Schema
public class House {
    @Key("people")
    @IgnoreValue(ifNull = true)
    private List<Human> people = new ArrayList<>();

    @Key("material")
    @Explanation("The material that makes up this house")
    @IgnoreValue(ifNull = true)
    @PrettyEnum
    private Material material = Material.WOOD;

    public List<Human> getPeople() {
        return people;
    }

    public Material getMaterial() {
        return material;
    }
}
