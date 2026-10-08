/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #content
// ###replace: package models;
package javaguide.ebean;

import io.ebean.DatabaseBuilder;
import io.ebean.event.ServerConfigStartup;

public class MyServerConfigStartup implements ServerConfigStartup {
  @Override
  public void onStart(DatabaseBuilder config) {
    config.databaseSequenceBatchSize(1);
  }
}
// #content
