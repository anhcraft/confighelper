package dev.anhcraft.confighelper;

import dev.anhcraft.confighelper.enums.Gender;
import dev.anhcraft.confighelper.exception.InvalidValueException;
import dev.anhcraft.confighelper.objects.*;
import dev.anhcraft.neep.NeepConfig;
import dev.anhcraft.neep.errors.NeepWriterException;
import org.junit.Test;

public class Main {
    @Test
    public void test(){
        Human human1 = new Human();
        human1.setGender(Gender.FEMALE);
        human1.setName("Alex");
        human1.setAge(20);
        Human human2 = new Human();
        human2.setGender(Gender.MALE);
        human2.setName("Steve");
        human2.setAge(23);
        Human human3 = new Human();
        human3.setGender(Gender.GAY);
        human3.setName("Endermen");
        human3.setAge(-99);
        House house1 = new House();
        house1.getPeople().add(human1);
        house1.getPeople().add(human2);
        House house2 = new House();
        house2.getPeople().add(human3);
        Town town = new Town();
        town.setHouses(new House[]{house1, house2});
        Country country = new Country();
        country.setName("Minecraft");
        country.getTowns().add(town);
        Earth earth = new Earth();
        earth.setAge(2000000);
        earth.getCountries().add(country);
        NeepConfig configuration1 = NeepConfig.create();
        ConfigHelper.writeConfig(configuration1, Earth.SCHEMA, earth, ConfigHelper.newOptions().ignoreFalse().ignoreZero());
        String conf = null;
        try {
            conf = configuration1.stringify();
        } catch (NeepWriterException e) {
            e.printStackTrace();
        }
        System.out.println(conf);
        NeepConfig configuration2 = NeepConfig.create();
        try {
            ConfigHelper.writeConfig(configuration2, Earth.SCHEMA, ConfigHelper.readConfig(configuration1, Earth.SCHEMA));
        } catch (InvalidValueException e) {
            e.printStackTrace();
        }
    }
}
