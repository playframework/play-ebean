/*
 * Copyright (C) 2009-2017 Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueType;
import play.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The raw parsed config from Ebean, as opposed to the EbeanConfig which actually requires starting database connection
 * pools to create.
 */
public class EbeanParsedConfig {

    private final String defaultDatasource;
    private final Map<String, List<String>> datasourceModels;

    public EbeanParsedConfig(String defaultDatasource, Map<String, List<String>> datasourceModels) {
        this.defaultDatasource = defaultDatasource;
        this.datasourceModels = datasourceModels;
    }

    public String getDefaultDatasource() {
        return defaultDatasource;
    }

    public Map<String, List<String>> getDatasourceModels() {
        return datasourceModels;
    }

    /**
     * Parse a play configuration.
     *
     * @param configuration play configuration
     * @return ebean parsed configuration
     *
     * @deprecated {@link play.Configuration} was deprecated in Play 2.6 in favor of {@link com.typesafe.config.Config}. Use {@link #parseFromConfig(Config)}
     */
    @Deprecated
    public static EbeanParsedConfig parseFromConfig(Configuration configuration) {
        return parseFromConfig(configuration.underlying());
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

        Map<String, List<String>> datasourceModels = new HashMap<>();

        if (config.hasPath(ebeanConfigKey)) {
            Config ebeanConfig = config.getConfig(ebeanConfigKey);
            ebeanConfig.root().forEach((key, raw) -> {
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
        return new EbeanParsedConfig(defaultDatasource, datasourceModels);
    }
}
