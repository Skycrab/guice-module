package com.google.code.guice.example;

import com.google.inject.*;
import com.google.inject.multibindings.OptionalBinder;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by yihaibo on 2019-10-12.
 * Guice默认绑定
 */
@Slf4j
public class GuiceOptionalBinderExample {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new FrameWorkModule(), new Module() {
      @Override
      public void configure(Binder binder) {
        //覆盖框架默认实现
        //如果需要传递参数，可以1.Inject 2.使用Provider
        OptionalBinder.newOptionalBinder(binder, Emit.class).setBinding().to(kafkaEmit.class);
      }
    });

    TestService testService = injector.getInstance(TestService.class);
    testService.test();
  }

  public static class TestService {
    private Emit emit;
    @Inject
    public TestService(Emit emit) {
      this.emit = emit;
    }

    public void test() {
      this.emit.emit("start TestService");
    }
  }


  //-------应用代码
  public static class kafkaEmit implements Emit {
    @Override
    public void emit(Object object) {
      log.info("kafkaEmit emit");
    }
  }


  //-------框架代码

  public static class FrameWorkModule implements Module {
    @Override
    public void configure(Binder binder) {
      //框架默认实现
      OptionalBinder.newOptionalBinder(binder, Emit.class).setDefault().to(HttpEmit.class);
    }
  }

  public interface Emit {
    void emit(Object object);
  }

  public static class HttpEmit implements Emit {
    @Override
    public void emit(Object object) {
      log.info("HttpEmit emit");
    }
  }
}
