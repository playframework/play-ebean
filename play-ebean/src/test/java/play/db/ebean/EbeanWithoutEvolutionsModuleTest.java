/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import static org.junit.Assert.assertEquals;

import io.ebean.DB;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import play.Application;
import play.api.db.evolutions.EvolutionsModule;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

public class EbeanWithoutEvolutionsModuleTest {

  @Test
  public void startsWithoutPlaysEvolutionsModule() {
    Application app =
        new GuiceApplicationBuilder()
            .disable(EvolutionsModule.class)
            .configure(
                Map.of(
                    "db.default.driver", "org.h2.Driver",
                    "db.default.url", "jdbc:h2:mem:play-ebean-without-evolutions",
                    "ebean.default", List.of("play.db.ebean.models.first.*")))
            .build();
    Helpers.running(app, () -> assertEquals("default", DB.getDefault().name()));
  }
}
