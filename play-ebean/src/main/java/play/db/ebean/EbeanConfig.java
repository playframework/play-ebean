/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import io.ebean.config.ServerConfig;

import java.util.Map;

public interface EbeanConfig {

    String defaultServer();

    Map<String, ServerConfig> serverConfigs();

}
