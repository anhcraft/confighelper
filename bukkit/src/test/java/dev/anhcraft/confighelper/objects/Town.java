package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.Explanation;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;

@Schema
public class Town {
    @Key("houses")
    @Explanation({
            "All houses in this town.",
            "The town must have at least one house."
    })
    @Validation(notNull = true, notEmptyArray = true)
    private House[] houses;

    public House[] getHouses() {
        return houses;
    }

    public void setHouses(House[] houses) {
        this.houses = houses;
    }
}
