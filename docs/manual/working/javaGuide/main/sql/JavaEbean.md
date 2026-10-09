<!--- Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com> -->

# Using the Ebean ORM

## Configuring Ebean

Play comes with the [Ebean](https://ebean-orm.github.io/) ORM. To enable it, add the Play Ebean plugin to your SBT plugins in `project/plugins.sbt`:

@[add-sbt-plugin](code/ebean.sbt)

> **Note**: see all available version [here](https://github.com/playframework/play-ebean#releases).

And then modify your `build.sbt` to enable the Play Ebean plugin:

@[enable-plugin](code/ebean.sbt)

### Configuring models

Play Ebean comes with two components, a runtime library that actually talks to the database, and an sbt plugin that enhances the compiled Java bytecode of your models for use with Ebean.  Both of these components need to be configured so that Ebean knows where your models are.

#### Configuring the runtime library

The runtime library can be configured by putting the list of packages and/or classes that your Ebean models live in your application configuration file.  For example, if all your models are in the `models` package, add the following to `conf/application.conf`:

```properties
ebean.default = ["models.*"]
```

This defines a `default` Ebean server, using the `default` data source, which must be properly configured. You can also override the name of the default Ebean server by configuring the `play.ebean.defaultDatasource` property. This might be useful if you want to use separate databases for testing and development. You can actually create as many Ebean servers you need, and explicitly define the mapped class for each server:

```properties
ebean.orders = ["models.Order", "models.OrderItem"]
ebean.customers =  ["models.Customer", "models.Address"]
```

In this example, we have access to two Ebean servers - each using its own database.

Each `ebean.` config line (as above) can map *any* classes that Ebean may be interested in registering (eg. `@Entity`/`Model` classes, `@Embeddable`s, custom `ScalarType`s and `CompoundType`s, `BeanPersistController`s, `BeanPersistListener`s, `BeanFinder`s, `ServerConfigStartup`s, etc). These can be individually listed separated by commas, and/or you can use the wildcard `.*`. For example, `models.*` registers with Ebean all classes within the models package that Ebean can make use of.

To customise the underlying Ebean Server configuration, you can either add a [`conf/application.yaml`](https://ebean.io/docs/intro/configuration/) file, or implement the `ServerConfigStartup` interface to programmatically manipulate the Ebean `DatabaseBuilder` before the server is initialised.

As an example, the fairly common problem of reducing the sequence batch size in order to minimise sequence gaps, could be solved quite simply with a class like this:

@[content](code/javaguide/ebean/MyServerConfigStartup.java)

Make sure that such a class is covered by an `ebean.` config line, for example by putting it into the `models` package. Play Ebean registers the configured classes with Ebean explicitly, which disables Ebean's own classpath scanning, so Ebean silently ignores any `ServerConfigStartup` that is not covered.

Note that Ebean doesn't read Play's `conf/application.conf`, so put Ebean's own settings (e.g. `ebean.migration.run`) into `conf/application.yaml`, which only Ebean reads, or into `conf/application.properties`. Play loads `conf/application.properties` as well, and Play Ebean ignores nested settings from there, like `ebean.migration.run`. However, a setting directly below `ebean.`, like `ebean.dumpMetricsOnShutdown=true`, looks like a list of models to Play Ebean, which only skips it because there's no database of that name. So prefer `conf/application.yaml` for such settings.

Note that Ebean will also make use of a `conf/orm.xml` file (if present), to configure `<entity-mappings>`.

#### Using a read replica

Ebean can run queries against a separate, read-only data source, typically a [read replica](https://ebean.io/docs/read-replicas/). This applies to queries executed outside of a transaction and to explicitly read-only transactions (e.g. `TxScope.required().setReadOnly(true)`). All writes and all queries inside of other transactions, including the ones of `@Transactional` actions, keep using the main data source.

To use a read replica, configure it as an additional Play database and set it as the `readOnlyDatasource` of the Ebean server:

```properties
db.default.url = "jdbc:postgresql://primary-host/app"
db.replica.url = "jdbc:postgresql://replica-host/app"
db.replica.hikaricp.readOnly = true

ebean.default = ["models.*"]
play.ebean.db.default.readOnlyDatasource = "replica"
```

Here `default` is the name of the Ebean server and `replica` the name of the Play database used for its read-only queries. Play manages the connection pool of the read replica like that of any other database, so all the usual `db` settings apply.

Ebean supports one read-only data source per Ebean server. To spread the read-only queries across multiple read replicas, use a JDBC URL that lists all of them (if supported by your JDBC driver, e.g. the PostgreSQL driver's `loadBalanceHosts` and `targetServerType` parameters), or point the URL to a load balancer or the reader endpoint of your database service.

#### Generating evolution scripts

In dev and test mode, Play Ebean generates the evolution script `conf/evolutions/<server>/1.sql` of each Ebean server from its models, so that Play's [evolutions](https://www.playframework.com/documentation/latest/Evolutions) can create the database schema. Whenever the models change, Play Ebean updates the script, as long as it starts with the `-- Created by Ebean DDL` comment. To write the evolutions yourself, remove this comment (both lines), then Play Ebean doesn't touch the file anymore.

You can turn off generating the scripts for all Ebean servers, and override that for single Ebean servers:

```properties
# Don't generate the scripts of the Ebean servers...
play.ebean.generateEvolutionsScripts = false
# ...except the one of the "default" Ebean server
play.ebean.db.default.generateEvolutionsScripts = true
```

This is independent of whether Play applies the evolutions to a database (`play.evolutions.db.<name>.enabled`). For example, turn off generating the script for a read-only database, or for a database whose evolution scripts come from the classpath (e.g. from a dependency), because a `conf/evolutions/<name>/1.sql` file takes precedence over these.

> For more information about Ebean, see the [Ebean documentation](https://ebean-orm.github.io/docs).

#### Configuring the sbt plugin

By default, the sbt plugin will attempt to load your `application.conf` file to discover what your models configuration is. This will work in a simple project setup, however, for projects that have multiple sub projects, where the `application.conf` file lives in a different project to where the ebean model classes live, this may not work. In this case you will need to manually specify the ebean models for each sub project that contains ebean models, using the `playEbeanModels` configuration item:

@[play-ebean-models](code/ebean.sbt)

In addition to configuring the models, you may wish to enable debug of the configuration. This can be done using `playEbeanDebugLevel`, with -1 being off, and 9 showing the most amount of debug:

@[play-ebean-debug](code/ebean.sbt)

You may also configure custom arguments for the ebean agent, this can be done using the `playEbeanAgentArgs` setting:

@[play-ebean-agent-args](code/ebean.sbt)

Finally, if you want to also enhance models in your tests, you can do this by configuring the ebean test configuration:

@[play-ebean-test](code/ebean.sbt)

## Using Model superclass

Ebean defines a convenient superclass for your Ebean model classes, `io.ebean.Model`. Here is a typical Ebean class, mapped in Play:

@[content](code/javaguide/ebean/Task.java)

> Play has been designed to generate getter/setter automatically, to ensure compatibility with libraries that expect them to be available at **runtime** (ORM, DataBinder, JSON Binder, etc). **If Play detects any user-written getter/setter in the Model, it will not generate getter/setter in order to avoid any conflict.**

> **Caveats:**

> (1) Because Ebean class enhancement occurs *after* compilation, **do not expect Ebean-generated getter/setters to be available at compilation time.** If you'd prefer to code with them directly, either add the getter/setters explicitly yourself, or ensure that your model classes are compiled before the remainder of your project, eg. by putting them in a separate subproject.

> (2) Enhancement of direct Ebean field access (enabling lazy loading) is only applied to Java classes, not to Scala. Thus, direct field access from Scala source files (including standard Play templates) does not invoke lazy loading, often resulting in empty (unpopulated) entity fields. To ensure the fields get populated, either (a) manually create getter/setters and call them instead, or (b) ensure the entity is fully populated *before* accessing the fields.

As you can see, we've added a `find` static field, defining a `Finder` for an entity of type `Task` with a `Long` identifier. This helper field is then used to simplify querying our model:

@[operations](code/javaguide/ebean/JavaEbeanTest.java)

> **Note:** Ebean also provides [query beans](https://ebean.io/docs/query/query-beans) for type-safe queries. Play Ebean doesn't set up their generation, and only enhances the code that uses them if it's in the packages of your models. You can set that up yourself, but it has limitations with incremental compilation, e.g. with sbt 1, queries using outdated query beans can compile and then fail at runtime. See [this comment on issue #61](https://github.com/playframework/play-ebean/issues/61#issuecomment-6089771595) for the configuration it needs, the versions it was tested with, and when to do a clean build.

## Transactional actions

By default Ebean will use transactions. However these transactions will be created before and committed or rollbacked after every single query, update, create or delete, as you can see here:

@[transaction](code/javaguide/ebean/JavaEbeanTest.java)

So, if you want to do more than one action in the same transaction you can use `DB.execute` (or `DB.executeCall` to return a value):

@[txrunnable](code/javaguide/ebean/JavaEbeanTest.java)

If your class is an action, you can annotate your action method with `@play.db.ebean.Transactional` to compose your action method with an `Action` that will automatically manage a transaction:

@[annotation](code/javaguide/ebean/JavaEbeanTest.java)

The transaction gets committed as soon as the action returns its result, or rolled back if the action throws an exception. As Ebean binds the transaction to the current thread, it only covers the database operations that the action runs on its own thread, before it returns:

- Database operations that run on another thread, e.g. in a `CompletableFuture.supplyAsync`, don't run in the transaction, even if they start before the action returns. Ebean then runs each of them in a transaction of its own, so they aren't atomic together anymore.
- The same applies to the action method itself, if another action that runs between `@Transactional` and the action method calls it asynchronously, e.g. only once an asynchronous check completed.
- If the action throws an exception, the transaction gets rolled back. If it returns a `CompletionStage` that fails instead, even one that failed already, the transaction gets committed.

To run asynchronous database operations in a transaction, begin it on the thread that runs them, and run all database operations of the transaction synchronously in there, e.g. with `DB.executeCall` on an execution context meant for blocking database calls:

@[async](code/javaguide/ebean/TaskService.java)

Don't return a `CompletionStage` from the callable that runs in the transaction, as the transaction would get committed before that completes as well. The `DatabaseExecutionContext` is a [`CustomExecutionContext`](https://www.playframework.com/documentation/latest/JavaAsync#Using-CustomExecutionContext-and-ClassLoaderExecution) for a dispatcher that keeps the blocking database calls off Play's default thread pool:

@[execution-context](code/javaguide/ebean/DatabaseExecutionContext.java)

Configure the dispatcher in `application.conf`. As each of its threads blocks on a database connection, give it as many threads as the connection pool has connections:

```
# Number of database connections
fixedConnectionPool = 9

play.db.prototype.hikaricp {
  minimumIdle = ${fixedConnectionPool}
  maximumPoolSize = ${fixedConnectionPool}
}

# Worker threads matched to the connection pool
database.dispatcher {
  executor = "thread-pool-executor"
  throughput = 1
  thread-pool-executor {
    fixed-pool-size = ${fixedConnectionPool}
  }
}
```

Or if you want a more traditional approach you can begin, commit and rollback transactions explicitly:

@[traditional](code/javaguide/ebean/JavaEbeanTest.java)

## Using Ebean during application startup

Play Ebean creates the Ebean databases while the application starts, and Ebean's static API (like `DB.getDefault()`, but also the methods of `Model` and finders) only works afterwards. So if a component uses Ebean while it gets created, e.g. in the constructor of an eagerly bound component, or of one created via Guice's static injection, make it depend on `play.api.db.evolutions.DynamicEvolutions`. Play Ebean binds that one, and it creates the databases:

@[startup](code/javaguide/ebean/TaskRepository.java)

Otherwise Ebean tries to create the database itself from its own configuration. That usually fails, e.g. with `Configuration error creating DataSource for the default Database`, and leaves Ebean unusable until the JVM restarts. A later error like `NoClassDefFoundError: Could not initialize class io.ebean.DbContext` only means that this initialization failed before, so look for the first error to find out why.

Note that this only makes sure that the databases exist, not that the evolutions have been applied. By default, in dev mode, Play only applies pending evolutions once you confirm them in the browser, and it can't show that page if the application fails to start, e.g. because a component queries a table that doesn't exist yet. So if a component uses the database schema while it gets created, e.g. to insert some initial data, make it depend on `play.api.db.evolutions.ApplicationEvolutions` instead. Play then checks the evolutions (and, if `autoApply` is enabled, applies them) before it creates the component, and as `ApplicationEvolutions` depends on `DynamicEvolutions`, the databases exist as well. In dev mode, Play starts the application even if evolutions still need to be applied, so only use the schema if `upToDate()` returns `true`:

@[startup-evolutions](code/javaguide/ebean/StartupDataLoader.java)

Once you apply the evolutions in the browser, Play reloads the application, which creates the component again, this time with `upToDate()` returning `true`. In test mode, Play applies the evolutions automatically, and in prod mode, it doesn't start the application while evolutions still need to be applied, unless they get applied automatically.

You can also let Play apply the evolutions automatically, limited to dev mode in your `build.sbt`:

```scala
PlayKeys.devSettings += "play.evolutions.db.default.autoApply" -> "true"
```

Be aware that this also applies down scripts automatically: when Play Ebean regenerates `conf/evolutions/default/1.sql` because your models changed, Play first reverts the previous version of that script, which drops the tables, so the data in your development database gets lost.
