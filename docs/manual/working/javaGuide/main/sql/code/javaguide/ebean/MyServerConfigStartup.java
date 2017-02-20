// #content
// ###replace: package models;
package javaguide.ebean;

import io.ebean.config.ServerConfig;
import io.ebean.event.ServerConfigStartup;

public class MyServerConfigStartup implements ServerConfigStartup {
    public void onStart(ServerConfig serverConfig) {
        serverConfig.setDatabaseSequenceBatchSize(1);
    }
}
// #content