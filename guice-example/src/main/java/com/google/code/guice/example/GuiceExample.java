package com.google.code.guice.example;

import com.google.inject.*;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by yihaibo on 2019-09-27.
 *
 * guice与spring的区别
 *
 * guice觉得基于xml方式太过隐式，而自动装配又过于显示，guice基于两者之间，通过代码控制，较为克制
 *
 */
@Slf4j
public class GuiceExample {
    public static void main( String[] args ) throws Exception {
        Injector injector = Guice.createInjector(new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(ProduceService.class).to(KafkaPrduceService.class);
                binder.bind(String.class).annotatedWith(Names.named("server")).toInstance("localhost:9002");
                binder.bind(String.class).annotatedWith(Names.named("topic")).toInstance("test");
            }
        });

        ProduceService produce = injector.getInstance(ProduceService.class);
        produce.produce("hello guice");
    }

    public interface ProduceService {
        void produce(Object msg);
    }

    @Singleton
    public static class KafkaPrduceService implements ProduceService {
        private String server;
        private String topic;

        @Inject
        public KafkaPrduceService(@Named("server") String server, @Named("topic") String topic) {
            this.server = server;
            this.topic = topic;
        }
        @Override
        public void produce(Object msg) {
            log.info("produce {}-{}-{}", server, topic, msg);
        }
    }
}
