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

package com.google.code.jersey;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.code.guice.common.utils.JvmUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.joda.time.Period;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;

/**
 */
@Getter
@EqualsAndHashCode
@ToString
public class ServerConfig
{
  public static final int DEFAULT_GZIP_INFLATE_BUFFER_SIZE = 4096;

  /**
   * The ServerConfig is normally created using {@link com.google.code.guice.jsonconfig.JsonConfigProvider} binding.
   *
   * This constructor is provided for callers that need to create a ServerConfig object with specific field values.
   */
  public ServerConfig(
      String host,
      int port,
      int numThreads,
      int queueSize,
      boolean enableRequestLimit,
      @NotNull Period maxIdleTime,
      long defaultQueryTimeout,
      long maxScatterGatherBytes,
      long maxQueryTimeout,
      int maxRequestHeaderSize,
      @NotNull Period gracefulShutdownTimeout,
      @NotNull Period unannouncePropagationDelay,
      int inflateBufferSize,
      int compressionLevel
  )
  {
    this.host = host;
    this.port = port;
    this.numThreads = numThreads;
    this.queueSize = queueSize;
    this.enableRequestLimit = enableRequestLimit;
    this.maxIdleTime = maxIdleTime;
    this.defaultQueryTimeout = defaultQueryTimeout;
    this.maxScatterGatherBytes = maxScatterGatherBytes;
    this.maxQueryTimeout = maxQueryTimeout;
    this.maxRequestHeaderSize = maxRequestHeaderSize;
    this.gracefulShutdownTimeout = gracefulShutdownTimeout;
    this.unannouncePropagationDelay = unannouncePropagationDelay;
    this.inflateBufferSize = inflateBufferSize;
    this.compressionLevel = compressionLevel;
  }

  public ServerConfig()
  {

  }

  @JsonProperty
  @NotNull
  private String host = "localhost";

  @JsonProperty
  @Max(0xffff)
  private int port = 8080;

  @JsonProperty
  @Min(1)
  private int numThreads = getDefaultNumThreads();

  @JsonProperty
  @Min(1)
  private int queueSize = Integer.MAX_VALUE;

  @JsonProperty
  private boolean enableRequestLimit = false;

  @JsonProperty
  @NotNull
  private Period maxIdleTime = new Period("PT5m");

  @JsonProperty
  @Min(0)
  private long defaultQueryTimeout = TimeUnit.MINUTES.toMillis(5);

  @JsonProperty
  @Min(1)
  private long maxScatterGatherBytes = Long.MAX_VALUE;

  @JsonProperty
  @Min(1)
  private long maxQueryTimeout = Long.MAX_VALUE;

  @JsonProperty
  private int maxRequestHeaderSize = 8 * 1024;

  @JsonProperty
  @NotNull
  private Period gracefulShutdownTimeout = Period.ZERO;

  @JsonProperty
  @NotNull
  private Period unannouncePropagationDelay = Period.ZERO;

  @JsonProperty
  @Min(0)
  private int inflateBufferSize = DEFAULT_GZIP_INFLATE_BUFFER_SIZE;

  @JsonProperty
  @Min(-1)
  @Max(9)
  private int compressionLevel = Deflater.DEFAULT_COMPRESSION;


  public static int getDefaultNumThreads()
  {
    return Math.max(10, (JvmUtils.getRuntimeInfo().getAvailableProcessors() * 17) / 16 + 2) + 30;
  }
}
