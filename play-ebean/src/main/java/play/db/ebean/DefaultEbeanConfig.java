/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.avaje.ebean.config.ServerConfig;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Environment;
import play.db.DBApi;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.*;

/**
 * Ebean server configuration.
 */
@Singleton
public class DefaultEbeanConfig implements EbeanConfig {

    private static final Logger log = LoggerFactory.getLogger(DefaultEbeanConfig.class);

    private final String defaultServer;
    private final Map<String, ServerConfig> serverConfigs;

    public DefaultEbeanConfig(String defaultServer, Map<String, ServerConfig> serverConfigs) {
        this.defaultServer = defaultServer;
        this.serverConfigs = serverConfigs;
    }

    @Override
    public String defaultServer() {
        return defaultServer;
    }

    @Override
    public Map<String, ServerConfig> serverConfigs() {
        return serverConfigs;
    }

    @Singleton
    public static class EbeanConfigParser implements Provider<EbeanConfig> {

        private final Configuration configuration;
        private final Environment environment;
        private final DBApi dbApi;

        @Inject
        public EbeanConfigParser(Configuration configuration, Environment environment, DBApi dbApi) {
            this.configuration = configuration;
            this.environment = environment;
            this.dbApi = dbApi;
        }

        @Override
        public EbeanConfig get() {
            return parse();
        }

        /**
         * Reads the configuration and creates config for Ebean servers.
         */
        public EbeanConfig parse() {

            Config config = configuration.underlying();
            Config playEbeanConfig = config.getConfig("play.ebean");
            String defaultServer = playEbeanConfig.getString("defaultDatasource");
            String ebeanConfigKey = playEbeanConfig.getString("config");

            Map<String, ServerConfig> serverConfigs = new HashMap<String, ServerConfig>();

            if (config.hasPath(ebeanConfigKey)) {
                Config ebeanConfig = config.getConfig(ebeanConfigKey);
                Configuration conf = new Configuration(ebeanConfig);

                for (String key: ebeanConfig.root().keySet()) {

                    ServerConfig serverConfig = new ServerConfig();
                    serverConfig.setName(key);
                    serverConfig.loadFromProperties();
                    try {
                        serverConfig.setDataSource(new WrappingDatasource(dbApi.getDatabase(key).getDataSource()));
                    } catch(Exception e) {
                        throw conf.reportError(
                            key,
                            e.getMessage(),
                            e
                        );
                    }

                    if (defaultServer.equals(key)) {
                        serverConfig.setDefaultServer(true);
                    }

                    ConfigValue raw = ebeanConfig.getValue(key);
                    List<String> toLoad;
                    if (raw.valueType() == ConfigValueType.STRING) {
                        // Support legacy comma separated string
                        toLoad = Arrays.asList(((String) raw.unwrapped()).split(","));
                    } else {
                        toLoad = ebeanConfig.getStringList(key);
                    }

                    Set<String> classes = new HashSet<String>();
                    for (String load: toLoad) {
                        load = load.trim();
                        if (load.endsWith(".*")) {
                            classes.addAll(play.libs.Classpath.getTypes(environment, load.substring(0, load.length()-2)));
                        } else {
                            classes.add(load);
                        }
                    }
                    for (String clazz: classes) {
                        try {
                            serverConfig.addClass(Class.forName(clazz, true, environment.classLoader()));
                        } catch (Throwable e) {
                            throw conf.reportError(
                                key,
                                "Cannot register class [" + clazz + "] in Ebean server",
                                e
                            );
                        }
                    }

                    serverConfigs.put(key, serverConfig);
                }
            }

            return new DefaultEbeanConfig(defaultServer, serverConfigs);
        }

        /**
         * <code>DataSource</code> wrapper to ensure that every retrieved connection has auto-commit disabled.
         */
        static class WrappingDatasource implements javax.sql.DataSource {

            public java.sql.Connection wrap(java.sql.Connection connection) throws java.sql.SQLException {
                connection.setAutoCommit(false);
                return connection;
            }

            // --

            final javax.sql.DataSource wrapped;

            public WrappingDatasource(javax.sql.DataSource wrapped) {
                this.wrapped = wrapped;
            }

            public java.sql.Connection getConnection() throws java.sql.SQLException {
                return wrap(wrapped.getConnection());
            }

            public java.sql.Connection getConnection(String username, String password) throws java.sql.SQLException {
                return wrap(wrapped.getConnection(username, password));
            }

            public int getLoginTimeout() throws java.sql.SQLException {
                return wrapped.getLoginTimeout();
            }

            public java.io.PrintWriter getLogWriter() throws java.sql.SQLException {
                return wrapped.getLogWriter();
            }

            public void setLoginTimeout(int seconds) throws java.sql.SQLException {
                wrapped.setLoginTimeout(seconds);
            }

            public void setLogWriter(java.io.PrintWriter out) throws java.sql.SQLException {
                wrapped.setLogWriter(out);
            }

            public boolean isWrapperFor(Class<?> iface) throws java.sql.SQLException {
                return wrapped.isWrapperFor(iface);
            }

            public <T> T unwrap(Class<T> iface) throws java.sql.SQLException {
                return wrapped.unwrap(iface);
            }

            public java.util.logging.Logger getParentLogger() {
                return null;
            }

        }

    }

}
