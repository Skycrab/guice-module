package com.google.code.guice.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.code.guice.jsonconfig.JsonConfigModule;
import com.google.code.guice.jsonconfig.JsonConfigProvider;
import com.google.inject.*;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Properties;

/**
 * Created by yihaibo on 2019-09-29.
 * json自动装配
 */
public class GuiceJsonConfigExample {
  public static void main( String[] args ) {
    Injector injector = Guice.createInjector(new JsonConfigModule(), new Module() {
      @Override
      public void configure(Binder binder) {
        JsonConfigProvider.bind(binder, "druid.server", DruidServerConfig.class);
      }

      @Provides
      @Singleton
      /**
       * Properties代码注入需注意必须为string
       */
      Properties provideProperties() {
        Properties props = new Properties();
        props.put("druid.server.hostname", "0.0.0.0");
        props.put("druid.server.port", "3333");
        return props;
      }

      @Provides @Singleton
      ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
      }

    });

    DruidServerConfig druidServerConfig = injector.getInstance(DruidServerConfig.class);

    System.out.println(druidServerConfig.port);
    System.out.println(druidServerConfig.hostname);
  }


  public static class DruidServerConfig {
    @JsonProperty
    @NotNull
    public String hostname = null;
    @JsonProperty @Min(1025) public int port = 8080;
  }

}
