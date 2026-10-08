/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #startup
// ###replace: package repositories;
package javaguide.ebean;

import io.ebean.DB;
import io.ebean.Database;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import play.api.db.evolutions.DynamicEvolutions;

@Singleton
public class TaskRepository {

  private final Database database;

  @Inject
  public TaskRepository(DynamicEvolutions ebeanDatabases) {
    // Play Ebean has created its Ebean databases by now
    this.database = DB.getDefault();
  }
}
// #startup
