/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import play.Mode;
import play.api.Configuration;
import play.api.Environment;

/**
 * Given a classloader, load the models configuration.
 *
 * <p>This is used by the ebean sbt plugin to get the same models configuration that will be loaded
 * by the app.
 */
public class ModelsConfigLoader implements Function<ClassLoader, Map<String, List<String>>> {

  @Override
  public Map<String, List<String>> apply(ClassLoader classLoader) {
    // Using TEST mode is the only way to load configuration without failing if application.conf
    // doesn't exist
    Environment env = new Environment(new File("."), classLoader, Mode.TEST.asScala());
    Configuration config = Configuration.load(env);
    return EbeanParsedConfig.parseFromConfig(config.underlying()).getDatasourceModels();
  }
}
