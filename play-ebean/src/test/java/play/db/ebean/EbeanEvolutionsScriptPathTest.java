/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.junit.Assert.assertEquals;

import io.ebean.DB;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

/**
 * Checks that Play applies the generated evolution script, wherever {@code
 * play.evolutions.db.<name>.path} puts it.
 */
public class EbeanEvolutionsScriptPathTest {

  private static final AtomicInteger databases = new AtomicInteger();

  /** Contains the application's root folder {@code app}, and the folders next to it. */
  @Rule public TemporaryFolder folder = new TemporaryFolder();

  private File root() {
    return new File(folder.getRoot(), "app");
  }

  /**
   * Runs the application, and returns the number of rows of the given table, which only works if
   * Play applied an evolution script that creates it.
   */
  private int run(Map<String, Object> config, String table) {
    root().mkdirs();
    GuiceApplicationBuilder builder =
        new GuiceApplicationBuilder()
            .in(root())
            .configure(
                Map.of(
                    "db.default.driver",
                    "org.h2.Driver",
                    "db.default.url",
                    "jdbc:h2:mem:play-ebean-path-" + databases.incrementAndGet(),
                    "ebean.default",
                    List.of("play.db.ebean.models.first.*"),
                    "play.evolutions.db.default.autoApply",
                    true))
            .configure(config);
    int[] rows = new int[1];
    Helpers.running(
        builder.build(),
        () ->
            rows[0] =
                DB.byName("default")
                    .sqlQuery("select count(*) as c from " + table)
                    .findOne()
                    .getInteger("c"));
    return rows[0];
  }

  /** Returns the evolution scripts in the temporary folder, relative to it. */
  private List<Path> scripts() throws IOException {
    Path base = folder.getRoot().toPath();
    try (Stream<Path> files = Files.walk(base)) {
      return files
          .filter(file -> file.getFileName().toString().equals("1.sql"))
          .map(base::relativize)
          .collect(Collectors.toList());
    }
  }

  private void assertGeneratesScript(Map<String, Object> config, String script) throws IOException {
    assertEquals(0, run(config, "first_model"));
    assertEquals(List.of(Path.of(script)), scripts());
  }

  @Test
  public void defaultPath() throws IOException {
    assertGeneratesScript(Map.of(), "app/conf/evolutions/default/1.sql");
  }

  @Test
  public void folderInConf() throws IOException {
    assertGeneratesScript(
        Map.of("play.evolutions.db.default.path", "migrations"),
        "app/conf/migrations/default/1.sql");
  }

  @Test
  public void folderInProject() throws IOException {
    assertGeneratesScript(
        Map.of("play.evolutions.db.default.path", "./migrations"), "app/migrations/default/1.sql");
  }

  @Test
  public void folderNextToProject() throws IOException {
    assertGeneratesScript(
        Map.of("play.evolutions.db.default.path", "../migrations"), "migrations/default/1.sql");
  }

  @Test
  public void absoluteFolder() throws IOException {
    String path = new File(folder.getRoot(), "migrations").getAbsolutePath();
    assertGeneratesScript(
        Map.of("play.evolutions.db.default.path", path), "migrations/default/1.sql");
  }

  @Test
  public void keepsHandWrittenScriptInAbsoluteFolder() throws IOException {
    File migrations = new File(folder.getRoot(), "migrations");
    Path script = new File(migrations, "default/1.sql").toPath();
    String handWritten =
        String.join(
            "\n",
            "-- !Ups",
            "create table first_model (id bigint not null primary key);",
            "create table hand_written (id bigint);",
            "",
            "-- !Downs",
            "drop table hand_written;",
            "drop table first_model;",
            "");
    Files.createDirectories(script.getParent());
    Files.write(script, handWritten.getBytes(StandardCharsets.UTF_8));

    assertEquals(
        0,
        run(
            Map.of("play.evolutions.db.default.path", migrations.getAbsolutePath()),
            "hand_written"));
    assertEquals(handWritten, new String(Files.readAllBytes(script), StandardCharsets.UTF_8));
    assertEquals(List.of(Path.of("migrations/default/1.sql")), scripts());
  }
}
