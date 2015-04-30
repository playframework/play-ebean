/*
 * Copyright (C) 2009-2015 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
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

    public static EbeanParsedConfig parseFromConfig(Configuration configuration) {
        Config config = configuration.underlying();
        Config playEbeanConfig = config.getConfig("play.ebean");
        String defaultDatasource = playEbeanConfig.getString("defaultDatasource");
        String ebeanConfigKey = playEbeanConfig.getString("config");

        Map<String, List<String>> datasourceModels = new HashMap<>();

        if (config.hasPath(ebeanConfigKey)) {
            Config ebeanConfig = config.getConfig(ebeanConfigKey);

            for (String key : ebeanConfig.root().keySet()) {

                ConfigValue raw = ebeanConfig.getValue(key);
                List<String> models;
                if (raw.valueType() == ConfigValueType.STRING) {
                    // Support legacy comma separated string
                    models = Arrays.asList(((String) raw.unwrapped()).split(","));
                } else {
                    models = ebeanConfig.getStringList(key);
                }

                datasourceModels.put(key, models);

            }
        }
        return new EbeanParsedConfig(defaultDatasource, datasourceModels);
    }
}
