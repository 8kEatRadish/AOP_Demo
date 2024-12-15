package com.shawn.krouter;

import java.util.ArrayList;

public class Hello {
    public final String a = "a";
    public final long aLong = Long.MAX_VALUE;

    public long getAlong() {
        return aLong;
    }

    private void load(ArrayList<Integer> arrayList, int age, String name, long birthday, boolean sex) {
        System.out.println(arrayList.get(0) + age + name + birthday + sex);
    }
}
