/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class EbeanEvolutionsScriptsTest {

  @Rule public TemporaryFolder rootPath = new TemporaryFolder();

  private GuiceApplicationBuilder appBuilder(Map<String, Object> config) {
    return new GuiceApplicationBuilder()
        .in(rootPath.getRoot())
        .configure(
            Map.of(
                "db.first.driver", "org.h2.Driver",
                "db.first.url", "jdbc:h2:mem:play-ebean-first",
                "db.second.driver", "org.h2.Driver",
                "db.second.url", "jdbc:h2:mem:play-ebean-second",
                "ebean.first", List.of("play.db.ebean.models.first.*"),
                "ebean.second", List.of("play.db.ebean.models.second.*"),
                "play.ebean.defaultDatasource", "first",
                // Generating the scripts doesn't depend on Play applying evolutions
                "play.evolutions.enabled", false))
        .configure(config);
  }

  /** Runs the application and returns for which Ebean servers it generated evolution scripts. */
  private List<Boolean> generatedScripts(Map<String, Object> config) {
    Helpers.running(appBuilder(config).build(), () -> {});
    return List.of(scriptExists("first"), scriptExists("second"));
  }

  private boolean scriptExists(String server) {
    return new File(rootPath.getRoot(), "conf/evolutions/" + server + "/1.sql").isFile();
  }

  @Test
  public void generatesScriptsByDefault() {
    assertEquals(List.of(true, true), generatedScripts(Map.of()));
  }

  @Test
  public void doesNotGenerateScriptsWhenDisabled() {
    assertEquals(
        List.of(false, false),
        generatedScripts(Map.of("play.ebean.generateEvolutionsScripts", false)));
  }

  @Test
  public void doesNotGenerateScriptOfServerWhenDisabledForServer() {
    assertEquals(
        List.of(true, false),
        generatedScripts(Map.of("play.ebean.db.second.generateEvolutionsScripts", false)));
  }

  @Test
  public void generatesScriptOfServerWhenEnabledForServer() {
    assertEquals(
        List.of(false, true),
        generatedScripts(
            Map.of(
                "play.ebean.generateEvolutionsScripts", false,
                "play.ebean.db.second.generateEvolutionsScripts", true)));
  }

  @Test
  public void failsForUnknownEbeanServer() {
    GuiceApplicationBuilder builder =
        appBuilder(Map.of("play.ebean.db.unknown.generateEvolutionsScripts", false));
    Exception e = assertThrows(Exception.class, builder::build);
    assertThat(e.getMessage(), containsString("play.ebean.db.unknown"));
    assertThat(e.getMessage(), containsString("There is no Ebean server 'unknown'"));
  }
}
