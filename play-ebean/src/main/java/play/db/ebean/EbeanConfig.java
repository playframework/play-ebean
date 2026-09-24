/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import io.ebean.DatabaseBuilder;
import java.util.Map;

public interface EbeanConfig {

  String defaultServer();

  Map<String, DatabaseBuilder.Settings> serverConfigs();

  boolean generateEvolutionsScripts();
}
