package com.google.code.guice.example;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import java.util.List;

/**
 * Created by yihaibo on 2019-09-29.
 * Guice模块绑定覆盖
 */
public class GuiceOverrideExample {
  public static void main(String[] args) {

    List<Module> builtIns = ImmutableList.of(binder -> {
      binder.bind(Service.class).to(BuiltinService.class);
    });

//    List<Module> customs = ImmutableList.of();

    List<Module> customs = ImmutableList.of(binder -> {
      binder.bind(Service.class).to(CustomService.class);
    });

    Injector injector = Guice.createInjector(Modules.override(builtIns).with(customs));

    FrameWork frameWork = injector.getInstance(FrameWork.class);
    frameWork.start();
  }

  public static class FrameWork {
    private Service service;

    @Inject
    public FrameWork(Service service) {
      this.service = service;
    }

    public void start() {
      this.service.run();
    }
  }

  public interface Service {
    void run();
  }

  public static class BuiltinService implements Service {
    @Override
    public void run() {
      System.out.println("BuiltinService");
    }
  }

  public static class CustomService implements Service {
    @Override
    public void run() {
      System.out.println("CustomService");
    }
  }
}
