package dev.anhcraft.confighelper;

import dev.anhcraft.confighelper.objects.Family;
import dev.anhcraft.confighelper.objects.Furniture;
import dev.anhcraft.confighelper.objects.House;
import dev.anhcraft.confighelper.objects.Person;
import org.junit.Test;

import java.io.File;

public class DocTest {
    @Test
    public void doc(){
        new ConfigDoc()
                .withSchemaOf(Family.class)
                .withSchemaOf(Furniture.class)
                .withSchemaOf(House.class)
                .withSchemaOf(Person.class)
                .generate(new File("docs"));
    }
}
