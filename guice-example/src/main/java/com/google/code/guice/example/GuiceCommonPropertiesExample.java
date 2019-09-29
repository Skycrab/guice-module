package com.google.code.guice.example;

import com.google.code.guice.common.PropertiesModule;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.Properties;

/**
 * Created by yihaibo on 2019-09-29.
 */
public class GuiceCommonPropertiesExample {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new PropertiesModule(ImmutableList.of("log4j.properties")));
    Properties properties = injector.getInstance(Properties.class);
    System.out.println(properties.getProperty("user.dir"));
    System.out.println(properties.getProperty("log4j.rootLogger"));
  }
}
