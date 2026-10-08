/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.event.ShutdownManager;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class EbeanShutdownTest {

  @Test
  public void ebeanShutdownHookDoesNotShutDownDatabasesBeforeApplicationStops() throws Exception {
    Application app =
        new GuiceApplicationBuilder()
            .configure(
                Map.of(
                    "db.default.driver",
                    "org.h2.Driver",
                    "db.default.url",
                    "jdbc:h2:mem:play-ebean-shutdown",
                    "ebean.default",
                    List.of(),
                    // Without models, Ebean searches the classpath and finds the test models, so
                    // don't
                    // write their evolutions script into the working directory
                    "play.ebean.generateEvolutionsScripts",
                    false))
            .build();
    Helpers.start(app);
    Database database = DB.getDefault();
    try {
      // On SIGTERM, Ebean's JVM shutdown hook runs while Play still serves in-flight requests
      ShutdownManager.shutdown();
      assertEquals(Integer.valueOf(1), database.backgroundExecutor().submit(() -> 1).get());
    } finally {
      Helpers.stop(app);
    }
    assertThrows(
        RejectedExecutionException.class, () -> database.backgroundExecutor().submit(() -> 1));
  }
}
