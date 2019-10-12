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

package com.google.code.jersey.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.code.guice.common.utils.JvmUtils;
import com.google.code.guice.common.utils.RuntimeInfo;
import com.google.code.guice.common.utils.StringUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 */
@Path("/status")
public class StatusResource
{
  private final Properties properties;

  @Inject
  public StatusResource(Properties properties)
  {
    this.properties = properties;
  }

  @GET
  @Path("/properties")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> getProperties()
  {
    Map<String, String> allProperties = Maps.fromProperties(properties);
    Set<String> hidderProperties = ImmutableSet.of();
    return Maps.filterEntries(allProperties, (entry) -> !hidderProperties.contains(entry.getKey()));
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Status doGet(
      @Context final HttpServletRequest req
  )
  {
    return new Status();
  }

  /**
   * This is an unsecured endpoint, defined as such in UNSECURED_PATHS in the service initiailization files
   * (e.g. CliOverlord, CoordinatorJettyServerInitializer)
   */
  @GET
  @Path("/health")
  @Produces(MediaType.APPLICATION_JSON)
  public boolean getHealth()
  {
    return true;
  }

  public static class Status
  {
    final String version;
    final Memory memory;

    public Status()
    {
      this.version = getCurrentVersion();
      this.memory = new Memory(JvmUtils.getRuntimeInfo());
    }

    private String getCurrentVersion()
    {
      return Status.class.getPackage().getImplementationVersion();
    }

    @JsonProperty
    public String getVersion()
    {
      return version;
    }

    @JsonProperty
    public Memory getMemory()
    {
      return memory;
    }

    @Override
    public String toString()
    {
      StringBuilder output = new StringBuilder();
      String lineSeparator = System.lineSeparator();
      output.append(StringUtils.format("Current version - %s", version)).append(lineSeparator).append(lineSeparator);

      return output.toString();
    }
  }

  public static class Memory
  {
    final long maxMemory;
    final long totalMemory;
    final long freeMemory;
    final long usedMemory;
    final long directMemory;

    public Memory(RuntimeInfo runtime)
    {
      maxMemory = runtime.getMaxHeapSizeBytes();
      totalMemory = runtime.getTotalHeapSizeBytes();
      freeMemory = runtime.getFreeHeapSizeBytes();
      usedMemory = totalMemory - freeMemory;

      long directMemory = -1;
      try {
        directMemory = runtime.getDirectMemorySizeBytes();
      }
      catch (UnsupportedOperationException ignore) {
        // querying direct memory is not supported
      }
      this.directMemory = directMemory;
    }

    @JsonProperty
    public long getMaxMemory()
    {
      return maxMemory;
    }

    @JsonProperty
    public long getTotalMemory()
    {
      return totalMemory;
    }

    @JsonProperty
    public long getFreeMemory()
    {
      return freeMemory;
    }

    @JsonProperty
    public long getUsedMemory()
    {
      return usedMemory;
    }

    @JsonProperty
    public long getDirectMemory()
    {
      return directMemory;
    }
  }
}
