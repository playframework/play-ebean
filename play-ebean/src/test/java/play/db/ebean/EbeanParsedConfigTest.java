/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import org.junit.*;

public class EbeanParsedConfigTest {

  private EbeanParsedConfig parse(Map<String, ?> config) {
    return EbeanParsedConfig.parseFromConfig(
        ConfigFactory.parseMap(config).withFallback(ConfigFactory.defaultReference()));
  }

  @Test
  public void defaultConfig() {
    EbeanParsedConfig config = parse(Collections.emptyMap());
    assertThat(config.getDefaultDatasource(), equalTo("default"));
    assertThat(config.getDatasourceModels().size(), equalTo(0));
    assertThat(config.generateEvolutionsScripts(), equalTo(Boolean.TRUE));
    assertThat(config.getReadOnlyDatasources().size(), equalTo(0));
    assertThat(config.getGenerateEvolutionsScriptsByServer().size(), equalTo(0));
  }

  @Test
  public void withDataSources() {
    EbeanParsedConfig config =
        parse(
            ImmutableMap.of(
                "ebean.default", Arrays.asList("a", "b"),
                "ebean.other", Collections.singletonList("c")));
    assertThat(config.getDatasourceModels().size(), equalTo(2));
    assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
    assertThat(config.getDatasourceModels().get("other"), hasItems("c"));
  }

  @Test
  public void ignoresEbeanSettings() {
    // Ebean's own settings, e.g. from conf/application.properties, which Play loads too
    EbeanParsedConfig config =
        parse(
            ImmutableMap.of(
                "ebean.default",
                Collections.singletonList("a"),
                "ebean.other.databasePlatformName",
                "h2",
                "ebean.migration.run",
                false,
                "ebean.dumpMetricsOnShutdown",
                true));
    assertThat(config.getDatasourceModels().keySet(), equalTo(Collections.singleton("default")));
    assertThat(config.getDatasourceModels().get("default"), hasItems("a"));
  }

  @Test
  public void commaSeparatedModels() {
    EbeanParsedConfig config = parse(ImmutableMap.of("ebean.default", "a,b"));
    assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
  }

  @Test
  public void customDefault() {
    EbeanParsedConfig config = parse(ImmutableMap.of("play.ebean.defaultDatasource", "custom"));
    assertThat(config.getDefaultDatasource(), equalTo("custom"));
  }

  @Test
  public void disableGenerateEvolutionsScripts() {
    EbeanParsedConfig config =
        parse(ImmutableMap.of("play.ebean.generateEvolutionsScripts", false));
    assertThat(config.generateEvolutionsScripts(), equalTo(Boolean.FALSE));
  }

  @Test
  public void disableGenerateEvolutionsScriptsForServer() {
    EbeanParsedConfig config =
        parse(ImmutableMap.of("play.ebean.db.other.generateEvolutionsScripts", false));
    assertThat(config.generateEvolutionsScripts("default"), equalTo(Boolean.TRUE));
    assertThat(config.generateEvolutionsScripts("other"), equalTo(Boolean.FALSE));
  }

  @Test
  public void enableGenerateEvolutionsScriptsForServer() {
    EbeanParsedConfig config =
        parse(
            ImmutableMap.of(
                "play.ebean.generateEvolutionsScripts", false,
                "play.ebean.db.other.generateEvolutionsScripts", true));
    assertThat(config.generateEvolutionsScripts("default"), equalTo(Boolean.FALSE));
    assertThat(config.generateEvolutionsScripts("other"), equalTo(Boolean.TRUE));
  }

  @Test
  public void readOnlyDatasources() {
    EbeanParsedConfig config =
        parse(ImmutableMap.of("play.ebean.db.default.readOnlyDatasource", "replica"));
    assertThat(config.getReadOnlyDatasources().size(), equalTo(1));
    assertThat(config.getReadOnlyDatasources().get("default"), equalTo("replica"));
  }

  @Test
  public void customConfig() {
    EbeanParsedConfig config =
        parse(
            ImmutableMap.of(
                "play.ebean.config",
                "my.custom",
                "my.custom.default",
                Collections.singletonList("a")));
    assertThat(config.getDatasourceModels().size(), equalTo(1));
    assertThat(config.getDatasourceModels().get("default"), hasItems("a"));
  }
}
