/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import io.ebean.config.DatabaseConfig;

import java.util.Map;

public interface EbeanConfig {

    String defaultServer();

    Map<String, DatabaseConfig> serverConfigs();

}
