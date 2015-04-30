<!--- Copyright (C) 2009-2013 Typesafe Inc. <http://www.typesafe.com> -->
# Using the Ebean ORM

## Configuring Ebean

Play comes with the [Ebean](https://ebean-orm.github.io/) ORM. To enable it, add the Play Ebean plugin to your SBT plugins in `project/plugins.sbt`:

```scala
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "1.0.0")
```

And then modify your `build.sbt` to enable the Play Ebean plugin:

```scala
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, SbtEbean)
```

Then add the following line to `conf/application.conf`:

```properties
ebean.default = ["models.*"]
```

This defines a `default` Ebean server, using the `default` data source, which must be properly configured. You can also override the name of the default Ebean server by configuring `ebeanconfig.datasource.default` property. This might be useful if you want to use separate databases for testing and development. You can actually create as many Ebean servers you need, and explicitly define the mapped class for each server:

```properties
ebean.orders = ["models.Order", "models.OrderItem"]
ebean.customers =  ["models.Customer", "models.Address"]
```

In this example, we have access to two Ebean servers - each using its own database.

Each `ebean.` config line (as above) can map *any* classes that Ebean may be interested in registering (eg. `@Entity`/`Model` classes, `@Embeddable`s, custom `ScalarType`s and `CompoundType`s, `BeanPersistController`s, `BeanPersistListener`s, `BeanFinder`s, `ServerConfigStartup`s, etc). These can be individually listed separated by commas, and/or you can use the wildcard `.*`. For example, `models.*` registers with Ebean all classes within the models package that Ebean can make use of.

To customise the underlying Ebean Server configuration, you can either add a `conf/ebean.properties` file, or create an instance of the `ServerConfigStartup` interface to programmatically manipulate the Ebean `ServerConfig` before the server is initialised.

As an example, the fairly common problem of reducing the sequence batch size in order to minimise sequence gaps, could be solved quite simply with a class like this:

@[content](code/javaguide/ebean/MyServerConfigStartup.java)

Note that Ebean will also make use of a `conf/orm.xml` file (if present), to configure `<entity-mappings>`.

> For more information about Ebean, see the [Ebean documentation](https://ebean-orm.github.io/docs).

## Using Model superclass

Ebean defines a convenient superclass for your Ebean model classes, `com.avaje.ebean.Model`. Here is a typical Ebean class, mapped in Play:

@[content](code/javaguide/ebean/Task.java)

> Play has been designed to generate getter/setter automatically, to ensure compatibility with libraries that expect them to be available at **runtime** (ORM, Databinder, JSON Binder, etc). **If Play detects any user-written getter/setter in the Model, it will not generate getter/setter in order to avoid any conflict.**

> **Caveats:**

> (1) Because Ebean class enhancement occurs *after* compilation, **do not expect Ebean-generated getter/setters to be available at compilation time.** If you'd prefer to code with them directly, either add the getter/setters explicitly yourself, or ensure that your model classes are compiled before the remainder of your project, eg. by putting them in a separate subproject.

> (2) Enhancement of direct Ebean field access (enabling lazy loading) is only applied to Java classes, not to Scala. Thus, direct field access from Scala source files (including standard Play templates) does not invoke lazy loading, often resulting in empty (unpopulated) entity fields. To ensure the fields get populated, either (a) manually create getter/setters and call them instead, or (b) ensure the entity is fully populated *before* accessing the fields.

As you can see, we've added a `find` static field, defining a `Finder` for an entity of type `Task` with a `Long` identifier. This helper field is then used to simplify querying our model:

@[operations](code/javaguide/ebean/JavaEbeanTest.java)

## Transactional actions

By default Ebean will use transactions. However this transactions will be created before and commited or rollbacked after every single query, update, create or delete, as you can see here:

@[transaction](code/javaguide/ebean/JavaEbeanTest.java)

So, if you want to do more than one action in the same transaction you can use TxRunnable and TxCallable:

@[txrunnable](code/javaguide/ebean/JavaEbeanTest.java)

If your class is an action, you can annotate your action method with `@play.db.ebean.Transactional` to compose your action method with an `Action` that will automatically manage a transaction:

@[annotation](code/javaguide/ebean/JavaEbeanTest.java)

Or if you want a more traditional approach you can begin, commit and rollback transactions explicitly:

@[traditional](code/javaguide/ebean/JavaEbeanTest.java)
