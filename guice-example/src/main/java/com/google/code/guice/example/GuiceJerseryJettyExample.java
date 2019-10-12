package com.google.code.guice.example;

import com.google.code.guice.jsonconfig.JsonConfigModule;
import com.google.code.jersey.Jerseys;
import com.google.code.jersey.ServerConfig;
import com.google.code.jersey.jetty.JerseyJettyServer;
import com.google.code.jersey.jetty.JettyServerModule;
import com.google.inject.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Properties;

/**
 * Created by yihaibo on 2019-10-08.
 */
@Slf4j
public class GuiceJerseryJettyExample {
  public static void main(String[] args) throws Exception {
    Injector injector = Guice.createInjector(new JsonConfigModule(), new JettyServerModule(), new Module() {
      @Override
      public void configure(Binder binder) {
        Jerseys.addResource(binder, IndexResource.class);
      }

      @Provides @Singleton
      Properties provideProperties() {
        Properties props = new Properties();
        props.put("server.http.host", "0.0.0.0");
        props.put("server.http.port", "9000");

        return props;
      }
    });

    JerseyJettyServer jerseyJettyServer = injector.getInstance(JerseyJettyServer.class);
    jerseyJettyServer.start();

    Thread.currentThread().join();
  }

  @Singleton
  @Path("/index")
  public static class IndexResource {
    private ServerConfig serverConfig;

    @Inject
    public IndexResource(ServerConfig serverConfig) {
      log.info("iiiiiii");
      this.serverConfig = serverConfig;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ServerConfig doGet(@Context final HttpServletRequest req) {
      return serverConfig;
    }
  }
}
