/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import io.ebean.DB;
import io.ebean.Database;
import io.ebean.Transaction;
import io.ebean.TxScope;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class EbeanReadOnlyDatasourceTest {

  private static GuiceApplicationBuilder appBuilder(Map<String, Object> config) {
    return new GuiceApplicationBuilder()
        .configure(
            Map.of(
                "db.default.driver", "org.h2.Driver",
                "db.default.url", "jdbc:h2:mem:play-ebean-primary",
                "db.replica.driver", "org.h2.Driver",
                "db.replica.url", "jdbc:h2:mem:play-ebean-replica",
                "ebean.default", List.of(),
                // Without models, Ebean searches the classpath and finds the test models, so don't
                // write their evolutions script into the working directory
                "play.ebean.generateEvolutionsScripts", false))
        .configure(config);
  }

  private static String currentDatabase(Database database) {
    return database.sqlQuery("select database()").mapToScalar(String.class).findOne();
  }

  @Test
  public void queriesOutsideOfTransactionsUseReadOnlyDatasource() {
    Application app =
        appBuilder(Map.of("play.ebean.db.default.readOnlyDatasource", "replica")).build();
    Helpers.start(app);
    try {
      Database database = DB.getDefault();
      assertEquals("PLAY-EBEAN-REPLICA", currentDatabase(database));
      try (Transaction transaction = database.beginTransaction()) {
        assertEquals("PLAY-EBEAN-PRIMARY", currentDatabase(database));
      }
      database.execute(
          TxScope.required().setReadOnly(true),
          () -> assertEquals("PLAY-EBEAN-REPLICA", currentDatabase(database)));
    } finally {
      Helpers.stop(app);
    }
  }

  @Test
  public void queriesUseMainDatasourceByDefault() {
    Application app = appBuilder(Map.of()).build();
    Helpers.start(app);
    try {
      assertEquals("PLAY-EBEAN-PRIMARY", currentDatabase(DB.getDefault()));
    } finally {
      Helpers.stop(app);
    }
  }

  @Test
  public void failsForUnknownPlayDatabase() {
    GuiceApplicationBuilder builder =
        appBuilder(Map.of("play.ebean.db.default.readOnlyDatasource", "unknown"));
    Exception e = assertThrows(Exception.class, builder::build);
    assertThat(e.getMessage(), containsString("play.ebean.db.default.readOnlyDatasource"));
    assertThat(e.getMessage(), containsString("There is no Play database 'unknown'"));
  }

  @Test
  public void failsForUnknownEbeanServer() {
    GuiceApplicationBuilder builder =
        appBuilder(Map.of("play.ebean.db.unknown.readOnlyDatasource", "replica"));
    Exception e = assertThrows(Exception.class, builder::build);
    assertThat(e.getMessage(), containsString("play.ebean.db.unknown"));
    assertThat(e.getMessage(), containsString("There is no Ebean server 'unknown'"));
  }
}
