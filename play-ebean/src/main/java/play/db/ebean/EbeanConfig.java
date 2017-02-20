/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import io.ebean.config.ServerConfig;

import java.util.Map;

public interface EbeanConfig {

    String defaultServer();

    Map<String, ServerConfig> serverConfigs();

}
