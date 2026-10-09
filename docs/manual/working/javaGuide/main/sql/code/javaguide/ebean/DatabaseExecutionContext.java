/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

// #execution-context
// ###replace: package services;
package javaguide.ebean;

import akka.actor.ActorSystem;
import javax.inject.Inject;
import play.libs.concurrent.CustomExecutionContext;

public class DatabaseExecutionContext extends CustomExecutionContext {

  @Inject
  public DatabaseExecutionContext(ActorSystem actorSystem) {
    // The dispatcher configured as "database.dispatcher" in application.conf
    super(actorSystem, "database.dispatcher");
  }
}
// #execution-context
