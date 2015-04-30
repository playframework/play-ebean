package play.db.ebean;

import play.Configuration;
import play.Environment;
import play.Mode;

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
        Environment env = new Environment(new File("."), classLoader, Mode.PROD);
        Configuration config = Configuration.load(env);
        return EbeanParsedConfig.parseFromConfig(config).getDatasourceModels();
    }
}
