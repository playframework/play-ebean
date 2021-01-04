/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import play.Mode;
import play.api.Environment;
import play.api.Configuration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Given a classloader, load the models configuration.
 *
 * This is used by the ebean sbt plugin to get the same models configuration that will be loaded by the app.
 */
public class ModelsConfigLoader implements Function<ClassLoader, Map<String, List<String>>> {

    @Override
    public  Map<String, List<String>> apply(ClassLoader classLoader) {
        // Using TEST mode is the only way to load configuration without failing if application.conf doesn't exist
        Environment env = new Environment(new File("."), classLoader, Mode.TEST.asScala());
        Configuration config = Configuration.load(env);
        return EbeanParsedConfig.parseFromConfig(config.underlying()).getDatasourceModels();
    }
}
