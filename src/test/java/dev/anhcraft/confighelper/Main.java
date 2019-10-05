package dev.anhcraft.confighelper;

import com.google.common.collect.ImmutableList;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.schemas.FamilySchema;
import dev.anhcraft.confighelper.schemas.FurnitureSchema;
import dev.anhcraft.confighelper.schemas.HouseSchema;
import dev.anhcraft.confighelper.schemas.PersonSchema;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class Main {
    @Test
    public void test(){
        YamlConfiguration conf = new YamlConfiguration();
        HouseSchema houseSchema = new HouseSchema();
        FurnitureSchema furnitureSchema = new FurnitureSchema();
        furnitureSchema.id = 0;
        furnitureSchema.material = Material.CHEST;
        FamilySchema familySchema = new FamilySchema();
        PersonSchema ps = new PersonSchema();
        ps.name = "Alex";
        ps.age = 22;
        ps.jobs = ImmutableList.of("dev", "designer", "minecrafter");
        ps.notes = new String[]{
                "22/10: Ok myself, I wanna making my own server",
                "23/10: it is too boring, delete it tomorrow"
        };
        ps.favNumber = new double[]{ 9, 4 };
        familySchema.getMembers().add(ps);
        familySchema.getMembers().add(new PersonSchema());
        houseSchema.family = familySchema;
        houseSchema.furniture.add(furnitureSchema);
        YamlHelper.writeConfig(conf, HouseSchema.STRUCT, houseSchema);
        String s = conf.saveToString();
        try {
            houseSchema = YamlHelper.readConfig(conf, HouseSchema.STRUCT);
            YamlHelper.writeConfig(conf, HouseSchema.STRUCT, houseSchema);
            Assert.assertEquals(s, conf.saveToString());
            System.out.println(s);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
