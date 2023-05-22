/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #content
// ###replace: package models;
package javaguide.ebean;

import io.ebean.config.ServerConfig;
import io.ebean.event.ServerConfigStartup;

public class MyServerConfigStartup implements ServerConfigStartup {
  public void onStart(ServerConfig serverConfig) {
    serverConfig.setDatabaseSequenceBatchSize(1);
  }
}
// #content
