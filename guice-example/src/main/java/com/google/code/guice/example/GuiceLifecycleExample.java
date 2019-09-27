package com.google.code.guice.example;

import com.google.code.guice.lifecycle.*;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Hello world!
 *
 */
public class GuiceLifecycleExample
{
    public static void main( String[] args ) throws Exception
    {
        final Injector injector = Guice.createInjector(new LifecycleModule());

        final Bootstrap bootstrap = injector.getInstance(Bootstrap.class);
        bootstrap.run();
    }

    public static class Bootstrap{
        // 必须主动注入ManageLifecycle，否则需要通过Lifecycle.addHandler主动注册
        private PrintLifecycle printLifecycle;
        private Lifecycle lifecycle;
        @Inject
        public Bootstrap(Lifecycle lifecycle, PrintLifecycle printLifecycle) {
            this.lifecycle = lifecycle;
            this.printLifecycle = printLifecycle;
        }

        public void run() throws Exception {
            System.out.println("Bootstrap run");
            lifecycle.start();
            lifecycle.join();
        }
    }

    @ManageLifecycle
    @Slf4j
    public static class PrintLifecycle {
        @LifecycleStart
        public void start() {
            System.out.println("PrintLifecycle start");
        }

        @LifecycleStop
        public void stop()
        {
            System.out.println("PrintLifecycle stop");
        }
    }
}
