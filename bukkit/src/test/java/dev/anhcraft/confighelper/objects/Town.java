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

    @Key("ancient")
    @Explanation("Is this an ancient town?")
    private boolean ancient;

    public House[] getHouses() {
        return houses;
    }

    public void setHouses(House[] houses) {
        this.houses = houses;
    }

    public boolean isAncient() {
        return ancient;
    }

    public void setAncient(boolean ancient) {
        this.ancient = ancient;
    }
}
