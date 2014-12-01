// #content
// ###replace: package models;
package javaguide.ebean;

import com.avaje.ebean.config.ServerConfig;
import com.avaje.ebean.event.ServerConfigStartup;

public class MyServerConfigStartup implements ServerConfigStartup {
    public void onStart(ServerConfig serverConfig) {
        serverConfig.setDatabaseSequenceBatchSize(1);
    }
}
// #content