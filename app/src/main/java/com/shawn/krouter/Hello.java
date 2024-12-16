package com.shawn.krouter;

import java.io.File;
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

    public void pushConstLdc() {
        // 范围 [-1,5]
        int iconst = -1;
        // 范围 [-128,127]
        int bipush = 127;
        // 范围 [-32768,32767]
        int sipush = 32767;
        // 其他 int （超出范围）
        int ldc = 32768;
        String aconst = null;
        String IdcString = "测试IdcString";
    }

    public void store(int age, String name) {
        int temp = age + 2;
        String str = name;
    }

    public void calculate(int age) {
        int add = age + 1;
        int sub = age - 1;
        int mul = age * 2;
        int div = age / 3;
        int rem = age % 4;
        age++;
        age--;
    }


    public void updown() {
        int i = 10;
        double d = i;

        float f = 10f;
        long ong = (long) f;
    }


    public void newObject() {
        String name = new String("测试newObject");
        File file = new File("测试newObject.book");
        int[] ages = {};
    }

    int age;

    public int incAndGet() {
        return ++age;
    }


    public void lcmp(long a, long b, int c, int d) {
        if (a > b) {
        }
    }

    public void fi() {
        int a = 0;
        if (a == 0) {
            a = 10;
        } else {
            a = 20;
        }
    }

    public void compare() {
        int i = 10;
        int j = 20;
        System.out.println(i > j);
    }


    public void switchTest(int select) {
        int num;
        switch (select) {
            case 1:
                num = 10;
                break;
            case 2:
            case 3:
                num = 30;
                break;
            default:
                num = 40;
        }
    }


    public void testException() {
        try {
            int a = 1 / 0; // 这将导致除以零的异常
        } catch (ArithmeticException e) {
            System.out.println("发生算术异常");
        }
    }


    public void syncBlockMethod() {
        synchronized (this) {
            // 同步块体
        }
    }
}
