/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import com.google.common.collect.ImmutableMap;
import com.typesafe.config.ConfigFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.junit.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.*;


public class EbeanParsedConfigTest {

    private EbeanParsedConfig parse(Map<String, ? extends Object> config) {
        return EbeanParsedConfig.parseFromConfig(
                ConfigFactory.parseMap(config).withFallback(ConfigFactory.defaultReference())
        );
    }

    @Test
    public void defaultConfig() {
        EbeanParsedConfig config = parse(Collections.emptyMap());
        assertThat(config.getDefaultDatasource(), equalTo("default"));
        assertThat(config.getDatasourceModels().size(), equalTo(0));
    }

    @Test
    public void withDataSources() {
        EbeanParsedConfig config = parse(ImmutableMap.of(
                "ebean.default", Arrays.asList("a", "b"),
                "ebean.other", Collections.singletonList("c")
        ));
        assertThat(config.getDatasourceModels().size(), equalTo(2));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
        assertThat(config.getDatasourceModels().get("other"), hasItems("c"));
    }

    @Test
    public void commaSeparatedModels() {
        EbeanParsedConfig config = parse(ImmutableMap.of(
                "ebean.default", "a,b"
        ));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a", "b"));
    }

    @Test
    public void customDefault() {
        EbeanParsedConfig config = parse(ImmutableMap.of(
                "play.ebean.defaultDatasource", "custom"
        ));
        assertThat(config.getDefaultDatasource(), equalTo("custom"));
    }

    @Test
    public void customConfig() {
        EbeanParsedConfig config = parse(ImmutableMap.of(
                "play.ebean.config", "my.custom",
                "my.custom.default", Collections.singletonList("a")
        ));
        assertThat(config.getDatasourceModels().size(), equalTo(1));
        assertThat(config.getDatasourceModels().get("default"), hasItems("a"));
    }


}
