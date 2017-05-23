/*
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */
package play.db.ebean;


import play.Configuration;
import play.Environment;
import play.api.db.evolutions.DynamicEvolutions;
import play.db.DBApi;
import play.inject.ApplicationLifecycle;

/**
 * Classes for Java compile time dependency injection.
 */
public interface EBeanComponents {

    ApplicationLifecycle applicationLifecycle();

    Environment environment();

    Configuration configuration();

    DBApi dbApi();

    default DynamicEvolutions dynamicEvolutions() {
        return new EbeanDynamicEvolutions(ebeanConfig(), environment(), applicationLifecycle());
    }

    default EbeanConfig ebeanConfig() {
        return new DefaultEbeanConfig.EbeanConfigParser(configuration(), environment(), dbApi()).get();
    }
}
