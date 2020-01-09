package dev.anhcraft.confighelper.objects;

import dev.anhcraft.confighelper.annotation.Example;
import dev.anhcraft.confighelper.annotation.Explanation;
import dev.anhcraft.confighelper.annotation.Key;
import dev.anhcraft.confighelper.annotation.Schema;

@Schema
public class Ageable {
    @Key("age")
    @Explanation("The current age")
    @Example("age: 300")
    @Example("age: 500")
    @Example("age: 900")
    private int age;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
