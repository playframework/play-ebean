/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

package play.db.ebean;

import play.api.Configuration;
import play.api.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/** Injection module with default Ebean components. */
public class EbeanModule extends Module {

  @Override
  public Seq<Binding<?>> bindings(Environment environment, Configuration configuration) {
    return seq(
        bind(DynamicEvolutions.class).to(EbeanDynamicEvolutions.class).eagerly(),
        bind(EbeanConfig.class).toProvider(DefaultEbeanConfig.EbeanConfigParser.class).eagerly(),
        bind(EbeanLifecycle.class).toSelf().eagerly());
  }
}
