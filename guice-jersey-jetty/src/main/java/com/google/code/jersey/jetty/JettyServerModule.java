/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.google.code.jersey.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.code.guice.common.exceptions.RE;
import com.google.code.guice.jsonconfig.JsonConfigProvider;
import com.google.code.jersey.JSR311Resource;
import com.google.code.jersey.Jerseys;
import com.google.code.jersey.ServerConfig;
import com.google.code.jersey.exceptions.BadRequestExceptionMapper;
import com.google.code.jersey.exceptions.CustomExceptionMapper;
import com.google.code.jersey.exceptions.ForbiddenExceptionMapper;
import com.google.code.jersey.resource.StatusResource;
import com.google.common.primitives.Ints;
import com.google.inject.*;
import com.google.inject.servlet.GuiceFilter;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import com.sun.jersey.spi.container.servlet.WebConfig;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 */
@Slf4j
public class JettyServerModule extends JerseyServletModule
{
  private static final AtomicInteger ACTIVE_CONNECTIONS = new AtomicInteger();

  @Override
  protected void configureServlets()
  {
    Binder binder = binder();

    JsonConfigProvider.bind(binder, "server.http", ServerConfig.class);

    binder.bind(GuiceContainer.class).to(JerseyGuiceContainer.class);
    binder.bind(JerseyGuiceContainer.class).in(Scopes.SINGLETON);
    binder.bind(CustomExceptionMapper.class).in(Singleton.class);
    binder.bind(ForbiddenExceptionMapper.class).in(Singleton.class);
    binder.bind(BadRequestExceptionMapper.class).in(Singleton.class);

    serve("/*").with(JerseyGuiceContainer.class);

    Jerseys.addResource(binder, StatusResource.class);
    binder.bind(StatusResource.class).in(Singleton.class);
  }

  public static class JerseyGuiceContainer extends GuiceContainer
  {
    private final Set<Class<?>> resources;

    @Inject
    public JerseyGuiceContainer(
        Injector injector,
        @JSR311Resource Set<Class<?>> resources
    )
    {
      super(injector);
      this.resources = resources;

    }

    @Override
    protected ResourceConfig getDefaultResourceConfig(
        Map<String, Object> props, WebConfig webConfig
    )
    {
      return new DefaultResourceConfig(resources);
    }
  }

  @Provides
  @Singleton
  public JerseyJettyServer getServer(final Injector injector, final ServerConfig config)
  {
    return makeAndInitializeServer(injector, config);
  }

  @Provides
  @Singleton
  public JacksonJsonProvider getJacksonJsonProvider(ObjectMapper objectMapper)
  {
    final JacksonJsonProvider provider = new JacksonJsonProvider();
    provider.setMapper(objectMapper);
    return provider;
  }

  static JerseyJettyServer makeAndInitializeServer(
      Injector injector,
      ServerConfig config
  )
  {
    // adjusting to make config.getNumThreads() mean, "number of threads
    // that concurrently handle the requests".
    int numServerThreads = config.getNumThreads() + getMaxJettyAcceptorsSelectorsNum(config);

    final QueuedThreadPool threadPool;
    if (config.getQueueSize() == Integer.MAX_VALUE) {
      threadPool = new QueuedThreadPool();
      threadPool.setMinThreads(numServerThreads);
      threadPool.setMaxThreads(numServerThreads);
    } else {
      threadPool = new QueuedThreadPool(
          numServerThreads,
          numServerThreads,
          60000, // same default is used in other case when threadPool = new QueuedThreadPool()
          new LinkedBlockingQueue<>(config.getQueueSize())
      );
    }

    threadPool.setDaemon(true);

    final Server server = new Server(threadPool);

    // Without this bean set, the default ScheduledExecutorScheduler runs as non-daemon, causing lifecycle hooks to fail
    // to fire on main exit. Related bug: https://github.com/apache/incubator-druid/pull/1627
    server.addBean(new ScheduledExecutorScheduler("JettyScheduler", true), true);

    final List<ServerConnector> serverConnectors = new ArrayList<>();

    log.info("Creating http serverConnector with port [{}]", config.getPort());
    HttpConfiguration httpConfiguration = new HttpConfiguration();
    httpConfiguration.setRequestHeaderSize(config.getMaxRequestHeaderSize());
    final ServerConnector serverConnector = new ServerConnector(server, new HttpConnectionFactory(httpConfiguration));
    serverConnector.setHost(config.getHost());
    serverConnector.setPort(config.getPort());
    serverConnectors.add(serverConnector);

    final ServerConnector[] connectors = new ServerConnector[serverConnectors.size()];
    int index = 0;
    for (ServerConnector connector : serverConnectors) {
      connectors[index++] = connector;
      connector.setIdleTimeout(Ints.checkedCast(config.getMaxIdleTime().toStandardDuration().getMillis()));
      // workaround suggested in -
      // https://bugs.eclipse.org/bugs/show_bug.cgi?id=435322#c66 for jetty half open connection issues during failovers
      connector.setAcceptorPriorityDelta(-1);

      List<ConnectionFactory> monitoredConnFactories = new ArrayList<>();
      for (ConnectionFactory cf : connector.getConnectionFactories()) {
        monitoredConnFactories.add(new JettyMonitoringConnectionFactory(cf, ACTIVE_CONNECTIONS));
      }
      connector.setConnectionFactories(monitoredConnFactories);
    }

    server.setConnectors(connectors);
    final long gracefulStop = config.getGracefulShutdownTimeout().toStandardDuration().getMillis();
    if (gracefulStop > 0) {
      server.setStopTimeout(gracefulStop);
    }
    server.addLifeCycleListener(new LifeCycle.Listener()
    {
      @Override
      public void lifeCycleStarting(LifeCycle event)
      {
        log.debug("Jetty lifecycle starting [{}]", event.getClass());
      }

      @Override
      public void lifeCycleStarted(LifeCycle event)
      {
        log.debug("Jetty lifeycle started [{}]", event.getClass());
      }

      @Override
      public void lifeCycleFailure(LifeCycle event, Throwable cause)
      {
        log.error("Jetty lifecycle event failed [{}]", event.getClass(), cause);
      }

      @Override
      public void lifeCycleStopping(LifeCycle event)
      {
        log.debug("Jetty lifecycle stopping [{}]", event.getClass());
      }

      @Override
      public void lifeCycleStopped(LifeCycle event)
      {
        log.debug("Jetty lifecycle stopped [{}]", event.getClass());
      }
    });

    final ServletContextHandler root = new ServletContextHandler(ServletContextHandler.SESSIONS);
    root.addServlet(new ServletHolder(new DefaultServlet()), "/*");
    root.addFilter(GuiceFilter.class, "/*", null);
    final HandlerList handlerList = new HandlerList();
    // Do not change the order of the handlers that have already been added
    for (Handler handler : server.getHandlers()) {
      handlerList.addHandler(handler);
    }

    handlerList.addHandler(root);

    final StatisticsHandler statisticsHandler = new StatisticsHandler();
    statisticsHandler.setHandler(handlerList);
    server.setHandler(statisticsHandler);

    return new JerseyJettyServer() {
      @Override
      public void start() throws Exception {
        log.info("Starting Jetty Server...");
        server.start();
      }

      @Override
      public void stop() {
        try {
          final long unannounceDelay = config.getUnannouncePropagationDelay().toStandardDuration().getMillis();
          if (unannounceDelay > 0) {
            log.info("Waiting %s ms for unannouncement to propagate.", unannounceDelay);
            Thread.sleep(unannounceDelay);
          } else {
            log.debug("Skipping unannounce wait.");
          }
          log.info("Stopping Jetty Server...");
          server.stop();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new RE(e, "Interrupted waiting for jetty shutdown.");
        } catch (Exception e) {
          log.warn("Unable to stop Jetty server.", e);
        }
      }
    };
  }

  private static int getMaxJettyAcceptorsSelectorsNum(ServerConfig config)
  {
    // This computation is based on Jetty v9.3.19 which uses upto 8(4 acceptors and 4 selectors) threads per
    // ServerConnector
    // 只启用了http
    int numServerConnector = 1;
    return numServerConnector * 8;
  }
}
