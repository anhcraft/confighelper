package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.PrettyEnum;
import dev.anhcraft.confighelper.annotation.Schema;
import dev.anhcraft.confighelper.annotation.Validation;
import dev.anhcraft.confighelper.enums.Gender;

@Schema
public class Human extends Ageable {
    @Key("name")
    @Validation(notNull = true)
    private String name;

    @Key("gender")
    @Validation(notNull = true)
    @PrettyEnum
    private Gender gender;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }
}
