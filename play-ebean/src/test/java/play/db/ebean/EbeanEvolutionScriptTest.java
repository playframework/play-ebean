/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import io.ebean.Database;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import play.api.db.evolutions.DownScript;
import play.api.db.evolutions.Evolution;
import play.api.db.evolutions.UpScript;
import play.db.ebean.models.ddl.Author;
import play.db.ebean.models.ddl.Book;
import play.db.ebean.models.ddl.Publisher;
import play.db.evolutions.Evolutions;
import scala.jdk.javaapi.CollectionConverters;

public class EbeanEvolutionScriptTest {

  private static final List<String> PLATFORMS =
      List.of(
          "h2",
          "postgres",
          "mysql",
          "mariadb",
          "sqlserver16",
          "sqlserver17",
          "oracle",
          "sqlite",
          "db2luw",
          "db2zos",
          "db2fori",
          "hana",
          "nuodb",
          "hsqldb",
          "sqlanywhere",
          "clickhouse",
          "cockroach",
          "yugabyte");

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  /**
   * Creates an Ebean database for the platform, without connecting to one. It's registered, so that
   * shutting it down doesn't make Ebean create a default database, and must be shut down.
   */
  static Database offlineDatabase(String platform, String ddlHeader, Class<?>... classes) {
    io.ebean.DatabaseBuilder builder =
        Database.builder()
            .setName("ddl-offline-" + platform)
            .setDefaultServer(false)
            .setDbOffline(true)
            .setDatabasePlatformName(platform)
            .setDdlHeader(ddlHeader);
    for (Class<?> cls : classes) {
      builder.addClass(cls);
    }
    return builder.build();
  }

  /** Reads the script like Play reads evolution scripts. */
  private Evolution read(String script) throws IOException {
    File root = folder.newFolder();
    File file = new File(root, "evolutions/default/1.sql");
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), script.getBytes(StandardCharsets.UTF_8));
    try (URLClassLoader classLoader = new URLClassLoader(new URL[] {root.toURI().toURL()}, null)) {
      return CollectionConverters.asJava(
              Evolutions.fromClassLoader(classLoader).evolutions("default"))
          .get(0);
    }
  }

  private static List<String> ups(Evolution evolution) {
    return CollectionConverters.asJava(new UpScript(evolution).statements());
  }

  private static List<String> downs(Evolution evolution) {
    return CollectionConverters.asJava(new DownScript(evolution).statements());
  }

  @Test
  public void playRunsTheStatementsOfEbean() throws IOException {
    List<String> failures = new ArrayList<>();
    for (String platform : PLATFORMS) {
      Database database =
          offlineDatabase(platform, null, Author.class, Book.class, Publisher.class);
      try {
        CurrentModel ddl = new CurrentModel((SpiEbeanServer) database);
        Evolution evolution = read(EbeanDynamicEvolutions.generateEvolutionScript(database));

        assertEquals(
            platform + " Ups",
            EbeanDynamicEvolutions.comparable(
                EbeanDynamicEvolutions.ebeanStatements(ddl.getCreateDdl())),
            EbeanDynamicEvolutions.comparable(ups(evolution)));
        assertEquals(
            platform + " Downs",
            EbeanDynamicEvolutions.comparable(
                EbeanDynamicEvolutions.ebeanStatements(ddl.getDropAllDdl())),
            EbeanDynamicEvolutions.comparable(downs(evolution)));
      } catch (Exception | AssertionError e) {
        failures.add(platform + ": " + e);
      } finally {
        database.shutdown(false, false);
      }
    }
    assertEquals(List.of(), failures);
  }

  @Test
  public void keepsDdlThatPlayAlreadyRunsLikeEbean() {
    Database database = offlineDatabase("h2", null, Author.class);
    try {
      CurrentModel ddl = new CurrentModel((SpiEbeanServer) database);
      String script = EbeanDynamicEvolutions.generateEvolutionScript(database);

      assertTrue(script, script.contains(ddl.getCreateDdl()));
      assertTrue(script, script.contains(ddl.getDropAllDdl()));
    } finally {
      database.shutdown(false, false);
    }
  }

  @Test
  public void keepsTheDdlHeader() throws IOException {
    // Ebean's DdlParser would run both statements as one, but Play always split them. Ebean
    // resolves ${timestamp} anew each time it returns the header.
    for (String header :
        List.of(
            "SET LOCK_TIMEOUT 1500; SET DEFAULT_NULL_ORDERING HIGH;",
            "-- generated ${timestamp} by Ebean ${version}\n"
                + "SET LOCK_TIMEOUT 1500; SET DEFAULT_NULL_ORDERING HIGH;")) {
      Database database = offlineDatabase("h2", header, Publisher.class);
      try {
        String script = EbeanDynamicEvolutions.generateEvolutionScript(database);
        Evolution evolution = read(script);

        // The column comment of Publisher has semicolons, so the rest of the DDL gets rewritten
        assertThat(script, containsString("-- !split-semicolon: never"));
        List<String> headerStatements =
            List.of("SET LOCK_TIMEOUT 1500", "SET DEFAULT_NULL_ORDERING HIGH");
        assertEquals(
            header,
            headerStatements,
            EbeanDynamicEvolutions.comparable(ups(evolution).subList(0, 2)));
        assertEquals(
            header,
            headerStatements,
            EbeanDynamicEvolutions.comparable(downs(evolution).subList(0, 2)));
      } finally {
        database.shutdown(false, false);
      }
    }
  }

  @Test
  public void keepsTheDdlHeaderWithAnotherTimestamp() {
    String ddl =
        "-- generated 2026-10-10T08:53:57.869123Z\n"
            + "SET LOCK_TIMEOUT 1500; SET DEFAULT_NULL_ORDERING HIGH;\n"
            + "comment on column publisher.name is 'a; b';\n";

    assertEquals(
        "-- generated 2026-10-10T08:53:57.869123Z\n"
            + "SET LOCK_TIMEOUT 1500; SET DEFAULT_NULL_ORDERING HIGH;\n"
            + "-- !split-semicolon: never\n"
            + "comment on column publisher.name is 'a; b'\n"
            + "-- !split-semicolon: always\n"
            + ";\n\n",
        EbeanDynamicEvolutions.evolutionScript(
            ddl,
            "-- generated 2026-10-10T08:53:58Z\n"
                + "SET LOCK_TIMEOUT 1500; SET DEFAULT_NULL_ORDERING HIGH;"));
  }

  @Test
  public void keepsTheDdlWithoutTheDdlHeader() {
    // E.g. if a future Ebean version put the header elsewhere
    String ddl = "SET LOCK_TIMEOUT 1500;\ncomment on column publisher.name is 'a; b';\n";

    assertEquals(
        ddl, EbeanDynamicEvolutions.evolutionScript(ddl, "SET DEFAULT_NULL_ORDERING HIGH;"));
  }
}
