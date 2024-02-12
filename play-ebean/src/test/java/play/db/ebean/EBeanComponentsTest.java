/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import java.util.Collections;
import org.junit.Test;
import play.Application;
import play.ApplicationLoader;
import play.Environment;
import play.LoggerConfigurator;
import play.api.Play;
import play.components.BodyParserComponents;
import play.db.HikariCPComponents;
import play.filters.components.NoHttpFiltersComponents;
import play.mvc.Results;
import play.routing.RoutingDslComponentsFromContext;

public class EBeanComponentsTest {

  static class MyComponents extends RoutingDslComponentsFromContext
      implements NoHttpFiltersComponents,
          BodyParserComponents,
          EBeanComponents,
          HikariCPComponents {

    public MyComponents(ApplicationLoader.Context context) {
      super(context);
    }

    @Override
    public play.routing.Router router() {
      return routingDsl().GET("/").routingTo((req) -> Results.ok("Hello")).build();
    }
  }

  static class MyApplicationLoader implements ApplicationLoader {
    @Override
    public Application load(Context context) {
      LoggerConfigurator.apply(context.environment().classLoader())
          .ifPresent(
              lc -> {
                lc.configure(
                    context.environment(), context.initialConfig(), Collections.emptyMap());
              });

      return new MyComponents(context).application();
    }
  }

  @Test
  public void createComponents() {
    MyApplicationLoader loader = new MyApplicationLoader();
    Application application = loader.load(new ApplicationLoader.Context(Environment.simple()));

    Play.start(application.asScala());
    Play.stop(application.asScala());
  }
}
