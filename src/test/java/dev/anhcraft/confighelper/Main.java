package dev.anhcraft.confighelper;

import com.google.common.collect.ImmutableList;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.objects.Family;
import dev.anhcraft.confighelper.objects.Furniture;
import dev.anhcraft.confighelper.objects.House;
import dev.anhcraft.confighelper.objects.Person;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.Assert;
import org.junit.Test;

public class Main {
    @Test
    public void test(){
        YamlConfiguration conf = new YamlConfiguration();
        House house = new House();
        Furniture furniture = new Furniture();
        furniture.id = 0;
        furniture.material = Material.CHEST;
        Family family = new Family();
        Person ps = new Person();
        ps.name = "Alex";
        ps.age = 22;
        ps.jobs = ImmutableList.of("dev", "designer", "minecrafter");
        ps.notes = new String[]{
                "22/10: Ok myself, I wanna making my own server",
                "23/10: it is too boring, delete it tomorrow"
        };
        ps.favNumber = new double[]{ 9, 4 };
        family.getMembers().add(ps);
        family.getMembers().add(new Person());
        house.family = family;
        house.furniture.add(furniture);
        ConfigHelper.writeConfig(conf, House.STRUCT, house);
        String s = conf.saveToString();
        try {
            house = ConfigHelper.readConfig(conf, House.STRUCT);
            ConfigHelper.writeConfig(conf, House.STRUCT, house);
            Assert.assertEquals(s, conf.saveToString());
            System.out.println(s);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
