/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import io.ebean.config.ServerConfig;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
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

        private final Config config;
        private final Environment environment;
        private final DBApi dbApi;

        @Inject
        public EbeanConfigParser(Config config, Environment environment, DBApi dbApi) {
            this.config = config;
            this.environment = environment;
            this.dbApi = dbApi;
        }

        @Override
        public EbeanConfig get() {
            return parse();
        }

        /**
         * Reads the configuration and creates config for Ebean servers.
         *
         * @return a config for Ebean servers.
         */
        public EbeanConfig parse() {

            EbeanParsedConfig ebeanConfig = EbeanParsedConfig.parseFromConfig(config);

            Map<String, ServerConfig> serverConfigs = new HashMap<>();

            for (Map.Entry<String, List<String>> entry: ebeanConfig.getDatasourceModels().entrySet()) {
                String key = entry.getKey();

                ServerConfig serverConfig = new ServerConfig();
                serverConfig.setName(key);
                serverConfig.loadFromProperties();

                setServerConfigDataSource(key, serverConfig);

                if (!ebeanConfig.getDefaultDatasource().equals(key)) {
                    serverConfig.setDefaultServer(false);
                }

                Set<String> classes = getModelClasses(entry);
                addModelClassesToServerConfig(key, serverConfig, classes);

                serverConfigs.put(key, serverConfig);
            }

            return new DefaultEbeanConfig(ebeanConfig.getDefaultDatasource(), serverConfigs);
        }

        private void setServerConfigDataSource(String key, ServerConfig serverConfig) {
            try {
                serverConfig.setDataSource(new WrappingDatasource(dbApi.getDatabase(key).getDataSource()));
            } catch(Exception e) {
                throw new ConfigException.BadValue(
                        "ebean." + key,
                        e.getMessage(),
                        e
                );
            }
        }

        private void addModelClassesToServerConfig(String key, ServerConfig serverConfig, Set<String> classes) {
            for (String clazz: classes) {
                try {
                    serverConfig.addClass(Class.forName(clazz, true, environment.classLoader()));
                } catch (Exception e) {
                    throw new ConfigException.BadValue(
                        "ebean." + key,
                        "Cannot register class [" + clazz + "] in Ebean server",
                        e
                    );
                }
            }
        }

        private Set<String> getModelClasses(Map.Entry<String, List<String>> entry) {
            Set<String> classes = new HashSet<>();
            entry.getValue().forEach(load -> {
                load = load.trim();
                if (load.endsWith(".*")) {
                    classes.addAll(Classpath.getTypes(environment, load.substring(0, load.length()-2)));
                } else {
                    classes.add(load);
                }
            });

            return classes;
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

    /**
     * Set of utilities for classpath manipulation.  This class should not be used, as
     * it was part of the Plugin API system which no longer exists in Play.
     */
    private static class Classpath {

        /**
         * Scans the environment classloader to retrieve all types within a specific package.
         * <p>
         * This method is useful for some plug-ins, for example the EBean plugin will automatically detect all types
         * within the models package.
         * <p>
         * Note that it is better to specify a very specific package to avoid expensive searches.
         *
         * @param env         the Play environment.
         * @param packageName the root package to scan
         * @return a set of types names satisfying the condition
         */
        static Set<String> getTypes(Environment env, String packageName) {
            return getReflections(env, packageName).getStore().keys(TypeElementsScanner.class.getSimpleName());
        }
        private static Reflections getReflections(Environment env, String packageName) {
            // This is not supposed to happen very often, but just when starting the application.
            // So it should be okay to not have a cache.
            return new Reflections(getReflectionsConfiguration(packageName, env.classLoader()));
        }

        /**
         * Create {@link org.reflections.Configuration} object for given package name and class loader.
         *
         * @param packageName the root package to scan
         * @param classLoader class loader to be used in reflections
         * @return the configuration builder
         */
        private static ConfigurationBuilder getReflectionsConfiguration(String packageName, ClassLoader classLoader) {
            return new ConfigurationBuilder()
                    .addUrls(ClasspathHelper.forPackage(packageName, classLoader))
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(packageName + ".")))
                    .setScanners(new TypeElementsScanner(), new TypeAnnotationsScanner(), new SubTypesScanner());
        }

    }

}
