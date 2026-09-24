/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import com.typesafe.config.Config;
import java.util.List;
import play.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.inject.Binding;
import play.inject.Module;

/** Injection module with default Ebean components. */
public class EbeanModule extends Module {

  @Override
  public List<Binding<?>> bindings(final Environment environment, final Config config) {
    return List.of(
        bindClass(DynamicEvolutions.class).to(EbeanDynamicEvolutions.class).eagerly(),
        bindClass(EbeanConfig.class)
            .toProvider(DefaultEbeanConfig.EbeanConfigParser.class)
            .eagerly());
  }
}
