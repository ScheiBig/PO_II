package edu.jeznach.po2.temp;

import java.lang.reflect.InvocationTargetException;

public class Statics {

    private static class A {
        public static String _() {
            return "a";
        }
    }

    private static class B extends A {
        public static String _() {
            return "b";
        }
    }

    public static void main(String[] args)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println(
                A.class.getMethod("_").invoke(new B())
        );
    }
}
