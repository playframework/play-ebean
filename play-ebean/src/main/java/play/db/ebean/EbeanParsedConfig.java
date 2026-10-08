/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import play.Logger;

/**
 * The raw parsed config from Ebean, as opposed to the EbeanConfig which actually requires starting
 * database connection pools to create.
 */
public class EbeanParsedConfig {

  private static final Logger.ALogger LOGGER = Logger.of(EbeanParsedConfig.class);

  private final String defaultDatasource;

  private final Map<String, List<String>> datasourceModels;

  private final boolean generateEvolutionsScripts;

  private final Map<String, Boolean> generateEvolutionsScriptsByServer;

  private final Map<String, String> readOnlyDatasources;

  public EbeanParsedConfig(
      String defaultDatasource,
      Map<String, List<String>> datasourceModels,
      boolean generateEvolutionsScripts,
      Map<String, Boolean> generateEvolutionsScriptsByServer,
      Map<String, String> readOnlyDatasources) {
    this.defaultDatasource = defaultDatasource;
    this.datasourceModels = datasourceModels;
    this.generateEvolutionsScripts = generateEvolutionsScripts;
    this.generateEvolutionsScriptsByServer = generateEvolutionsScriptsByServer;
    this.readOnlyDatasources = readOnlyDatasources;
  }

  public EbeanParsedConfig(
      String defaultDatasource,
      Map<String, List<String>> datasourceModels,
      boolean generateEvolutionsScripts) {
    this(
        defaultDatasource,
        datasourceModels,
        generateEvolutionsScripts,
        Collections.emptyMap(),
        Collections.emptyMap());
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

  public boolean generateEvolutionsScripts() {
    return generateEvolutionsScripts;
  }

  /**
   * @param server the name of the Ebean server
   * @return whether to generate the evolutions script of the given Ebean server
   */
  public boolean generateEvolutionsScripts(String server) {
    return generateEvolutionsScriptsByServer.getOrDefault(server, generateEvolutionsScripts);
  }

  /**
   * @return the explicit settings whether to generate the evolutions scripts, keyed by Ebean server
   *     name.
   */
  public Map<String, Boolean> getGenerateEvolutionsScriptsByServer() {
    return generateEvolutionsScriptsByServer;
  }

  /**
   * @return the names of the Play databases the Ebean servers use for read-only queries, keyed by
   *     Ebean server name.
   */
  public Map<String, String> getReadOnlyDatasources() {
    return readOnlyDatasources;
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
    String defaultDatasource = playEbeanConfig.getString("defaultDatasource");
    String ebeanConfigKey = playEbeanConfig.getString("config");
    boolean generateEvolutionsScripts = playEbeanConfig.getBoolean("generateEvolutionsScripts");

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
                } else if (raw.valueType() == ConfigValueType.LIST) {
                  models = ebeanConfig.getStringList(key);
                } else {
                  // No models, but e.g. one of Ebean's own settings, which Play also loads from
                  // conf/application.properties
                  if (raw.origin().description().contains(".conf")) {
                    LOGGER.warn(
                        "Ignoring {}.{} ({}), as it's no list of models and Ebean doesn't read"
                            + " Play's configuration. Put Ebean's own settings into"
                            + " conf/application.yaml or conf/application.properties instead.",
                        ebeanConfigKey,
                        key,
                        raw.origin().description());
                  }
                  return;
                }

                datasourceModels.put(key, models);
              });
    }
    Map<String, Boolean> generateEvolutionsScriptsByServer = new HashMap<>();
    Map<String, String> readOnlyDatasources = new HashMap<>();
    Config serversConfig = playEbeanConfig.getConfig("db");
    serversConfig
        .root()
        .keySet()
        .forEach(
            server -> {
              Config serverConfig = serversConfig.getConfig(server);
              if (serverConfig.hasPath("generateEvolutionsScripts")) {
                generateEvolutionsScriptsByServer.put(
                    server, serverConfig.getBoolean("generateEvolutionsScripts"));
              }
              if (serverConfig.hasPath("readOnlyDatasource")) {
                readOnlyDatasources.put(server, serverConfig.getString("readOnlyDatasource"));
              }
            });

    return new EbeanParsedConfig(
        defaultDatasource,
        datasourceModels,
        generateEvolutionsScripts,
        generateEvolutionsScriptsByServer,
        readOnlyDatasources);
  }
}
