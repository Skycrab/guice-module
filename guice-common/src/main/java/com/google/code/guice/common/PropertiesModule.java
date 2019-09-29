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

package com.google.code.guice.common;

import com.google.inject.Binder;
import com.google.inject.Module;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;

/**
 * 加载properties配置(包括系统配置)
 */
@Slf4j
public class PropertiesModule implements Module
{
  private final List<String> propertiesFiles;

  /**
   * @param propertiesFiles 越往后优先级越高
   */
  public PropertiesModule(List<String> propertiesFiles)
  {
    this.propertiesFiles = propertiesFiles;
  }

  @Override
  public void configure(Binder binder)
  {
    final Properties fileProps = new Properties();
    Properties systemProps = System.getProperties();

    Properties props = new Properties(fileProps);
    props.putAll(systemProps);

    for (String propertiesFile : propertiesFiles) {
      InputStream stream = ClassLoader.getSystemResourceAsStream(propertiesFile);
      try {
        if (stream == null) {
          File workingDirectoryFile = new File(propertiesFile);
          if (workingDirectoryFile.exists()) {
            stream = new BufferedInputStream(new FileInputStream(workingDirectoryFile));
          }
        }

        if (stream != null) {
          log.info("Loading properties from {}", propertiesFile);
          try (final InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            fileProps.load(in);
          }
          catch (IOException e) {
            throw new RuntimeException(e);
          }
        }
      }
      catch (FileNotFoundException e) {
        log.error("This can only happen if the .exists() call lied.  That's f'd up.", e);
      }
      finally {
        try{
          stream.close();
        }catch (IOException e) {
          log.error("IOException thrown while closing Closeable.", e);
        }
      }
    }

    binder.bind(Properties.class).toInstance(props);
  }
}
