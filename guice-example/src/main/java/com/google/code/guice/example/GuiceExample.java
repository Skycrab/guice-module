package com.google.code.guice.example;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Created by yihaibo on 2019-09-27.
 *
 * guice与spring的区别
 *
 * guice觉得基于xml方式太过隐式，而自动装配又过于显示，guice基于两者之间，通过代码控制，较为克制
 *
 */
public class GuiceExample {
    public static void main( String[] args ) throws Exception {
        Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {

            }
        });

        A a = injector.getInstance(A.class);
        a.print();

    }

    public static class  A {
        public void print() {
            System.out.println("AAA");
        }
    }
}
