package com.shawn.krouter;

public class Hello2 {
    private String name;
    static String name2 = "name2Str";

    public static void main(String[] args) {
        print(name2);
        Hello2 w = new Hello2();
        print(w.name);
    }

    public static void print(String arg) {
        System.out.println(arg);
    }
}
