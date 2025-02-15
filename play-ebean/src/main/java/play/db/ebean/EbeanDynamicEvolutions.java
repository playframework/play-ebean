/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebeaninternal.api.SpiEbeanServer;
import io.ebeaninternal.dbmigration.model.CurrentModel;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import play.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.api.db.evolutions.Evolutions$;
import play.api.db.evolutions.EvolutionsConfig;
import play.inject.ApplicationLifecycle;

/** A Play module that automatically manages Ebean configuration. */
@Singleton
public class EbeanDynamicEvolutions extends DynamicEvolutions {

  private final EbeanConfig config;
  private final Environment environment;

  private final EvolutionsConfig evolutionsConfig;

  private final Map<String, Database> databases = new HashMap<>();

  @Inject
  public EbeanDynamicEvolutions(
      EbeanConfig config,
      Environment environment,
      ApplicationLifecycle lifecycle,
      EvolutionsConfig evolutionsConfig) {
    this.config = config;
    this.environment = environment;
    this.evolutionsConfig = evolutionsConfig;
    start();
    lifecycle.addStopHook(
        () -> {
          databases.forEach((key, database) -> database.shutdown(false, false));
          return CompletableFuture.completedFuture(null);
        });
  }

  /** Initialise the Ebean servers/databases. */
  public void start() {
    config
        .serverConfigs()
        .forEach((key, serverConfig) -> databases.put(key, DatabaseFactory.create(serverConfig)));
  }

  /** Generate evolutions. */
  @Override
  public void create() {
    if (environment.isProd()) {
      return;
    }
    if (!config.generateEvolutionsScripts()) {
      return;
    }
    config
        .serverConfigs()
        .forEach(
            (key, serverConfig) -> {
              String evolutionScript = generateEvolutionScript(databases.get(key));
              if (evolutionScript == null) {
                return;
              }
              File evolutions =
                  environment.getFile(
                      Evolutions$.MODULE$.fileName(
                          key, 1, evolutionsConfig.forDatasource(key).path()));
              try {
                String content = "";
                if (evolutions.exists()) {
                  content =
                      new String(Files.readAllBytes(evolutions.toPath()), StandardCharsets.UTF_8);
                }
                if (content.isEmpty()
                    || content.startsWith("# --- Created by Ebean DDL")
                    || content.startsWith("-- Created by Ebean DDL")) {
                  environment
                      .getFile(
                          Evolutions$.MODULE$.directoryName(
                              key, evolutionsConfig.forDatasource(key).path()))
                      .mkdirs();
                  if (!content.equals(evolutionScript)) {
                    Files.write(
                        evolutions.toPath(), evolutionScript.getBytes(StandardCharsets.UTF_8));
                  }
                }
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }

  /**
   * Helper method that generates the required evolution to properly run Ebean/DB.
   *
   * @param database the Database.
   * @return the complete migration generated by Ebean/DB.
   */
  public static String generateEvolutionScript(Database database) {
    return generateScript((SpiEbeanServer) database);
  }

  private static String generateScript(SpiEbeanServer spiServer) {
    CurrentModel ddl = new CurrentModel(spiServer);

    String ups = ddl.getCreateDdl();
    String downs = ddl.getDropAllDdl();

    if (ups == null || ups.trim().isEmpty()) {
      return null;
    }

    return "-- Created by Ebean DDL\r\n"
        + "-- To stop Ebean DDL generation, remove this comment (both lines) and start using Evolutions\r\n"
        + "\r\n"
        + "-- !Ups\r\n"
        + "\r\n"
        + ups
        + "\r\n"
        + "-- !Downs\r\n"
        + "\r\n"
        + downs;
  }
}
