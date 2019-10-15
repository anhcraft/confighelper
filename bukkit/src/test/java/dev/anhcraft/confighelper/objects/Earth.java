package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.ConfigSchema;
import dev.anhcraft.confighelper.annotation.Explanation;
import dev.anhcraft.confighelper.annotation.IgnoreValue;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema
public class Earth extends Ageable {
    public static final ConfigSchema<Earth> SCHEMA = ConfigSchema.of(Earth.class);

    @Key("countries")
    @Explanation("All countries on the world")
    @IgnoreValue(ifNull = true)
    private List<Country> countries = new ArrayList<>();

    public List<Country> getCountries() {
        return countries;
    }
}
