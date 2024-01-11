/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The raw parsed config from Ebean, as opposed to the EbeanConfig which actually requires starting
 * database connection pools to create.
 */
public class EbeanParsedConfig {
  private final String defaultDatasource;

  private final Map<String, List<String>> datasourceModels;

  private final Boolean generateEvolutionsScripts;

  public EbeanParsedConfig(
      String defaultDatasource,
      Map<String, List<String>> datasourceModels,
      Boolean generateEvolutionsScripts) {
    this.defaultDatasource = defaultDatasource;
    this.datasourceModels = datasourceModels;
    this.generateEvolutionsScripts = generateEvolutionsScripts;
  }

  public EbeanParsedConfig(String defaultDatasource, Map<String, List<String>> datasourceModels) {
    this(defaultDatasource, datasourceModels, true);
  }

  public String getDefaultDatasource() {
    return defaultDatasource;
  }

  public Map<String, List<String>> getDatasourceModels() {
    return datasourceModels;
  }

  public Boolean generateEvolutionsScripts() {
    return generateEvolutionsScripts;
  }

  /**
   * Parse a play configuration.
   *
   * @param config play configuration
   * @return ebean parsed configuration
   * @see com.typesafe.config.Config
   */
  public static EbeanParsedConfig parseFromConfig(Config config) {
    Config playEbeanConfig = config.getConfig("play.ebean");
    String ebeanConfigKey = playEbeanConfig.getString("config");
    String ebeanDefaultDatasource = playEbeanConfig.getString("defaultDatasource");
    Boolean ebeanGenerateEvolutionsScripts =
        playEbeanConfig.getBoolean("generateEvolutionsScripts");

    Map<String, List<String>> datasourceModels = new HashMap<>();

    if (config.hasPath(ebeanConfigKey)) {
      Config ebeanConfig = config.getConfig(ebeanConfigKey);
      ebeanConfig
          .root()
          .forEach(
              (key, raw) -> {
                List<String> models;
                if (raw.valueType() == ConfigValueType.STRING) {
                  // Support legacy comma separated string
                  models = Arrays.asList(((String) raw.unwrapped()).split(","));
                } else {
                  models = ebeanConfig.getStringList(key);
                }

                datasourceModels.put(key, models);
              });
    }
    return new EbeanParsedConfig(
        ebeanDefaultDatasource, datasourceModels, ebeanGenerateEvolutionsScripts);
  }
}
