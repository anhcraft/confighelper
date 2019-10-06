package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;

@Schema
public class Generation {
    @Key("level")
    private int level;
}
