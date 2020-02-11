/*
 * Copyright (C) Lightbend Inc. <http://www.typesafe.com>
 */
package play.db.ebean;


import org.junit.Test;
import play.*;
import play.routing.*;
import play.api.Play;
import play.components.BodyParserComponents;
import play.db.HikariCPComponents;
import play.filters.components.NoHttpFiltersComponents;
import play.mvc.Results;
import play.routing.RoutingDsl;

import java.util.Collections;

public class EBeanComponentsTest {

    class MyComponents extends RoutingDslComponentsFromContext
            implements NoHttpFiltersComponents, BodyParserComponents, EBeanComponents, HikariCPComponents {

        public MyComponents(ApplicationLoader.Context context) {
            super(context);
        }

        @Override
        public play.routing.Router router() {
            return routingDsl().GET("/").routingTo((req) ->
                    Results.ok("Hello")
            ).build();
        }
    }

    class MyApplicationLoader implements ApplicationLoader {
        @Override
        public Application load(Context context) {
            LoggerConfigurator.apply(context.environment().classLoader()).ifPresent(lc -> {
                lc.configure(context.environment(), context.initialConfig(), Collections.emptyMap());
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
