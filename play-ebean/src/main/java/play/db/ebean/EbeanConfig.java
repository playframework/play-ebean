/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import io.ebean.config.DatabaseConfig;
import java.util.Map;

public interface EbeanConfig {

  String defaultServer();

  Map<String, DatabaseConfig> serverConfigs();

  boolean generateEvolutionsScripts();

  /**
   * @param server the name of the Ebean server
   * @return whether to generate the evolutions script of the given Ebean server
   */
  default boolean generateEvolutionsScripts(String server) {
    return generateEvolutionsScripts();
  }
}
