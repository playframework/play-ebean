/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import io.ebean.event.ShutdownManager;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import javax.inject.Singleton;
import play.api.db.evolutions.DynamicEvolutions;
import play.inject.ApplicationLifecycle;

/** A Play module that automatically manages Ebean configuration. */
@Singleton
public class EbeanLifecycle extends DynamicEvolutions {
  @Inject
  public EbeanLifecycle(ApplicationLifecycle lifecycle) {
    ShutdownManager.deregisterShutdownHook();
    lifecycle.addStopHook(
        () -> {
          ShutdownManager.shutdown();
          return CompletableFuture.completedFuture(null);
        });
  }
}
