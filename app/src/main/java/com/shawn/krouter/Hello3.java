package com.shawn.krouter;

import java.util.ArrayList;
import java.util.List;

public class Hello3 {
    private void run() {
        List ls = new ArrayList();
        ls.add("item1");

        ArrayList als = new ArrayList();
        als.add("item2");
    }

    public static void print() {
        System.out.println("hello3");
    }

    public static void main(String[] args) {
        print();
        Hello3 invoke = new Hello3();
        invoke.run();
    }
}
