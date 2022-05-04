/*
 * Copyright (C) Lightbend Inc. <https://www.lightbend.com>
 */
package play.db.ebean;

import play.api.db.evolutions.DynamicEvolutions;
import play.components.ConfigurationComponents;
import play.db.DBComponents;

/**
 * Classes for Java compile time dependency injection.
 */
public interface EBeanComponents extends ConfigurationComponents, DBComponents {

    default DynamicEvolutions dynamicEvolutions() {
        return new EbeanDynamicEvolutions(ebeanConfig(), environment(), applicationLifecycle());
    }

    default EbeanConfig ebeanConfig() {
        return new DefaultEbeanConfig.EbeanConfigParser(config(), environment(), dbApi()).get();
    }
}
