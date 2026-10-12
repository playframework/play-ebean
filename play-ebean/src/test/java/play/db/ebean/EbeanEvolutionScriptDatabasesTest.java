/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assume.assumeTrue;

import io.ebean.Database;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import play.api.db.evolutions.EvolutionsReader;
import play.db.Databases;
import play.db.ebean.models.ddl.Author;
import play.db.ebean.models.ddl.Book;
import play.db.ebean.models.ddl.Publisher;
import play.db.evolutions.Evolutions;

/**
 * Applies the generated evolution scripts on real databases, and reverts them again.
 *
 * <p>The tests of databases other than H2 and SQLite only run when configured with environment
 * variables, e.g. {@code PLAY_EBEAN_TEST_MYSQL_URL}, {@code PLAY_EBEAN_TEST_MYSQL_USER} and {@code
 * PLAY_EBEAN_TEST_MYSQL_PASSWORD} or {@code PLAY_EBEAN_TEST_MYSQL_PASSWORD_FILE}.
 */
public class EbeanEvolutionScriptDatabasesTest {

  @Rule public TemporaryFolder folder = new TemporaryFolder();

  @Test
  public void h2() throws Exception {
    test("h2", "org.h2.Driver", "jdbc:h2:mem:play-ebean-ddl", null, null, true, false, true);
  }

  @Test
  public void sqlite() throws Exception {
    test(
        "sqlite",
        "org.sqlite.JDBC",
        "jdbc:sqlite:" + folder.newFile(),
        null,
        null,
        false,
        false,
        false);
  }

  @Test
  public void postgres() throws Exception {
    configured("POSTGRES", "postgres", "org.postgresql.Driver", true, false, true);
  }

  @Test
  public void mysql() throws Exception {
    configured("MYSQL", "mysql", "com.mysql.cj.jdbc.Driver", true, true, true);
  }

  @Test
  public void mariadb() throws Exception {
    configured("MARIADB", "mariadb", "org.mariadb.jdbc.Driver", true, true, true);
  }

  @Test
  public void sqlserver() throws Exception {
    configured(
        "SQLSERVER",
        "sqlserver17",
        "com.microsoft.sqlserver.jdbc.SQLServerDriver",
        true,
        true,
        false);
  }

  @Test
  public void oracle() throws Exception {
    configured("ORACLE", "oracle", "oracle.jdbc.OracleDriver", false, false, false);
  }

  private void configured(
      String name,
      String platform,
      String driver,
      boolean history,
      boolean procedures,
      boolean comments)
      throws Exception {
    String prefix = "PLAY_EBEAN_TEST_" + name + "_";
    String url = System.getenv(prefix + "URL");
    assumeTrue(prefix + "URL isn't set", url != null);
    String password = System.getenv(prefix + "PASSWORD");
    String passwordFile = System.getenv(prefix + "PASSWORD_FILE");
    if (passwordFile != null) {
      password = Files.readString(Path.of(passwordFile)).trim();
    }
    test(
        platform,
        driver,
        url,
        System.getenv(prefix + "USER"),
        password,
        history,
        procedures,
        comments);
  }

  private void test(
      String platform,
      String driver,
      String url,
      String user,
      String password,
      boolean history,
      boolean procedures,
      boolean comments)
      throws Exception {
    Map<String, Object> config = new HashMap<>();
    if (user != null) {
      config.put("username", user);
    }
    if (password != null) {
      config.put("password", password);
    }
    play.db.Database database = Databases.createFrom("ddl-" + platform, driver, url, config);
    try {
      // Registered, so that shutting it down doesn't make Ebean create a default database. Like in
      // applications, Ebean gets connections without auto-commit.
      Database ebean =
          Database.builder()
              .setName("ddl-" + platform)
              .setDefaultServer(false)
              .setDataSource(
                  new DefaultEbeanConfig.EbeanConfigParser.WrappingDatasource(
                      database.getDataSource()))
              .setDatabasePlatformName(platform)
              .addClass(Author.class)
              .addClass(Book.class)
              .addClass(Publisher.class)
              .build();
      try {
        EvolutionsReader reader =
            write(database.getName(), EbeanDynamicEvolutions.generateEvolutionScript(ebean));

        // Reverts what an earlier, interrupted run may have left behind
        Evolutions.cleanupEvolutions(database);
        // Applying the Ups a second time shows that the Downs left nothing behind
        for (int run = 1; run <= 2; run++) {
          Evolutions.applyEvolutions(database, reader);
          useModels(ebean, history);
          if (procedures) {
            useProcedure(database);
          }
          if (comments) {
            assertEquals(Publisher.NAME_COMMENT, nameComment(database));
          }
          Evolutions.cleanupEvolutions(database);
          assertThrows(Exception.class, () -> ebean.find(Author.class).findCount());
        }
      } finally {
        ebean.shutdown(false, false);
      }
    } finally {
      database.shutdown();
    }
  }

  private EvolutionsReader write(String database, String script) throws Exception {
    File root = folder.newFolder();
    File file = new File(root, "evolutions/" + database + "/1.sql");
    file.getParentFile().mkdirs();
    Files.write(file.toPath(), script.getBytes(StandardCharsets.UTF_8));
    return Evolutions.fromClassLoader(new URLClassLoader(new URL[] {root.toURI().toURL()}, null));
  }

  private static void useModels(Database ebean, boolean history) {
    Author author = new Author();
    author.setName("Ann; Smith");
    ebean.save(author);
    Book book = new Book();
    book.setTitle("First; draft");
    book.setAuthor(author);
    ebean.save(book);
    book.setTitle("Final");
    ebean.update(book);

    Book found = ebean.find(Book.class, book.getId());
    assertEquals("Final", found.getTitle());
    assertEquals("Ann; Smith", found.getAuthor().getName());
    if (history) {
      assertEquals(2, ebean.find(Book.class).setId(book.getId()).findVersions().size());
    }
    ebean.delete(book);
    ebean.delete(author);

    Publisher publisher = new Publisher();
    publisher.setName("Semicolon Press");
    ebean.save(publisher);
    assertEquals("Semicolon Press", ebean.find(Publisher.class, publisher.getId()).getName());
  }

  /** Reads the comment of the name column of Publisher, where JDBC reports comments. */
  private static String nameComment(play.db.Database database) throws Exception {
    try (Connection connection = database.getConnection()) {
      DatabaseMetaData metaData = connection.getMetaData();
      boolean upperCase = metaData.storesUpperCaseIdentifiers();
      try (ResultSet columns =
          metaData.getColumns(
              connection.getCatalog(),
              null,
              upperCase ? "PUBLISHER" : "publisher",
              upperCase ? "NAME" : "name")) {
        return columns.next() ? columns.getString("REMARKS") : null;
      }
    }
  }

  /** Uses one of the procedures that Ebean creates for its migrations. */
  private static void useProcedure(play.db.Database database) throws Exception {
    try (Connection connection = database.getConnection(true);
        Statement statement = connection.createStatement()) {
      statement.execute("create table procedure_test (id int, name varchar(10))");
      try {
        try (CallableStatement call =
            connection.prepareCall("{call usp_ebean_drop_column(?, ?)}")) {
          call.setString(1, "procedure_test");
          call.setString(2, "name");
          call.execute();
        }
        assertThrows(
            Exception.class, () -> statement.executeQuery("select name from procedure_test"));
      } finally {
        statement.execute("drop table procedure_test");
      }
    }
  }
}
