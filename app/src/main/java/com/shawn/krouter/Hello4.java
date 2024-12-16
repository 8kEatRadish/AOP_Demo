package com.shawn.krouter;

import java.util.function.Function;

public class Hello4 {
    public static void main(String[] args) {
        // 使用 Lambda 表达式定义一个函数
        Function<Integer, Integer> square = x -> x * x;

        // 调用这个函数
        int result = square.apply(5);

        System.out.println(result); // 输出 25
    }
}
