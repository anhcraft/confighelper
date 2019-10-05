package dev.anhcraft.confighelper;

import com.google.common.collect.ImmutableList;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.schemas.FamilySchema;
import dev.anhcraft.confighelper.schemas.HouseSchema;
import dev.anhcraft.confighelper.schemas.PersonSchema;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class Main {
    @Test
    public void test(){
        YamlConfiguration conf = new YamlConfiguration();
        HouseSchema houseSchema = new HouseSchema();
        FamilySchema familySchema = new FamilySchema();
        PersonSchema ps = new PersonSchema();
        ps.name = "Alex";
        ps.age = 22;
        ps.jobs = ImmutableList.of("dev", "designer", "minecrafter");
        familySchema.getMembers().add(ps);
        familySchema.getMembers().add(new PersonSchema());
        houseSchema.family = familySchema;
        YamlHelper.writeConfig(conf, HouseSchema.STRUCT, houseSchema);
        String s = conf.saveToString();
        try {
            houseSchema = YamlHelper.readConfig(conf, HouseSchema.STRUCT);
            YamlHelper.writeConfig(conf, HouseSchema.STRUCT, houseSchema);
            Assert.assertEquals(s, conf.saveToString());
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
