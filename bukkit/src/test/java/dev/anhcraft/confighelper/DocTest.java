package dev.anhcraft.confighelper;

import dev.anhcraft.confighelper.objects.*;
import org.junit.Test;

import java.io.File;

public class DocTest {
    @Test
    public void doc(){
        new ConfigDoc()
                .withSchema(Earth.SCHEMA)
                .withSchemaOf(Ageable.class)
                .withSchemaOf(Country.class)
                .withSchemaOf(Town.class)
                .withSchemaOf(House.class)
                .withSchemaOf(Human.class)
                .generate(new File("docs"));
    }
}
