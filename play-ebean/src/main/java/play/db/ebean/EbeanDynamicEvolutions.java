/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;

import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.dbmigration.DdlGenerator;
import com.avaje.ebeaninternal.api.SpiEbeanServer;
import play.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.inject.ApplicationLifecycle;
import play.libs.F;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * A Play module that automatically manages Ebean configuration.
 */
@Singleton
public class EbeanDynamicEvolutions extends DynamicEvolutions {

    private final EbeanConfig config;
    private final Environment environment;

    private final Map<String, EbeanServer> servers = new HashMap<String, EbeanServer>();

    @Inject
    public EbeanDynamicEvolutions(EbeanConfig config, Environment environment, ApplicationLifecycle lifecycle) {
        this.config = config;
        this.environment = environment;
        start();
        lifecycle.addStopHook(() -> {
            servers.forEach((database, server) -> server.shutdown(false, false));
            return F.Promise.<Void>pure(null);
        });
    }

    /**
     * Initialise the Ebean servers.
     */
    public void start() {
        for (Map.Entry<String, ServerConfig> entry : config.serverConfigs().entrySet()) {
            String key = entry.getKey();
            ServerConfig serverConfig = entry.getValue();
            servers.put(key, EbeanServerFactory.create(serverConfig));
        }
    }

    /**
     * Generate evolutions.
     */
    @Override
    public void create() {
        if (!environment.isProd()) {
            for (Map.Entry<String, ServerConfig> entry : config.serverConfigs().entrySet()) {
                String key = entry.getKey();
                ServerConfig serverConfig = entry.getValue();
                String evolutionScript = generateEvolutionScript(servers.get(key), serverConfig);
                if (evolutionScript != null) {
                    File evolutions = environment.getFile("conf/evolutions/" + key + "/1.sql");
                    try {
                        String content;
                        if (!evolutions.exists()) {
                            content = "";
                        } else {
                            content = new String(Files.readAllBytes(evolutions.toPath()), "utf-8");
                        }

                        if (content.isEmpty() || content.startsWith("# --- Created by Ebean DDL")) {
                            environment.getFile("conf/evolutions/" + key).mkdirs();
                            if (!content.equals(evolutionScript)) {
                                Files.write(evolutions.toPath(), evolutionScript.getBytes("utf-8"));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * Helper method that generates the required evolution to properly run Ebean.
     */
    public static String generateEvolutionScript(EbeanServer server, ServerConfig config) {
        DdlGenerator ddl = new DdlGenerator();
        ddl.setup((SpiEbeanServer) server, config);
        String ups = ddl.generateCreateDdl();
        String downs = ddl.generateDropDdl();

        if(ups == null || ups.trim().isEmpty()) {
            return null;
        }

        return (
            "# --- Created by Ebean DDL\r\n" +
            "# To stop Ebean DDL generation, remove this comment and start using Evolutions\r\n" +
            "\r\n" +
            "# --- !Ups\r\n" +
            "\r\n" +
            ups +
            "\r\n" +
            "# --- !Downs\r\n" +
            "\r\n" +
            downs
        );
    }

}
