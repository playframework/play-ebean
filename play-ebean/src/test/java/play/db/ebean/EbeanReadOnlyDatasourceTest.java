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
                "ebean.default", List.of()))
        .configure(config);
  }

  private static String currentDatabase(Database database) {
    return database.sqlQuery("select database()").mapToScalar(String.class).findOne();
  }

  @Test
  public void queriesOutsideOfTransactionsUseReadOnlyDatasource() {
    Application app =
        appBuilder(Map.of("play.ebean.readOnlyDatasources.default", "replica")).build();
    Helpers.start(app);
    try {
      Database database = DB.getDefault();
      assertEquals("PLAY-EBEAN-REPLICA", currentDatabase(database));
      try (Transaction transaction = database.beginTransaction()) {
        assertEquals("PLAY-EBEAN-PRIMARY", currentDatabase(database));
      }
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
        appBuilder(Map.of("play.ebean.readOnlyDatasources.default", "unknown"));
    Exception e = assertThrows(Exception.class, builder::build);
    assertThat(e.getMessage(), containsString("play.ebean.readOnlyDatasources.default"));
    assertThat(e.getMessage(), containsString("There is no Play database 'unknown'"));
  }

  @Test
  public void failsForUnknownEbeanServer() {
    GuiceApplicationBuilder builder =
        appBuilder(Map.of("play.ebean.readOnlyDatasources.unknown", "replica"));
    Exception e = assertThrows(Exception.class, builder::build);
    assertThat(e.getMessage(), containsString("play.ebean.readOnlyDatasources.unknown"));
    assertThat(e.getMessage(), containsString("There is no Ebean server 'unknown'"));
  }
}
