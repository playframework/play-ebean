/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #startup-evolutions
// ###replace: package services;
package javaguide.ebean;

import javax.inject.Inject;
import javax.inject.Singleton;
// ###insert: import models.Task;
import play.api.db.evolutions.ApplicationEvolutions;

@Singleton
public class StartupDataLoader {

  @Inject
  public StartupDataLoader(ApplicationEvolutions evolutions) {
    // Only use the schema once all evolutions have been applied. In dev mode, applying them in
    // the browser reloads the application, which creates this component again.
    if (evolutions.upToDate() && Task.find.query().findCount() == 0) {
      Task task = new Task();
      task.setName("Initial task");
      task.save();
    }
  }
}
// #startup-evolutions
