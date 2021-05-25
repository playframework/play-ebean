import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  object ScalaVersions {
    val scala212 = "2.12.13"
    val scala213 = "2.13.5"
  }

  object Versions {
    val play: String        = "2.8.8"
    val ebean               = "12.8.3"
    val ebeanAgent          = "12.8.3"
    val ebeanQueryBeanAgent = "10.1.3"
    val ebeanQueryBeanGen   = "12.8.3"
    val typesafeConfig      = "1.4.1"
  }

  val ebean = libraryDependencies ++= Seq(
    "io.ebean"           % "ebean"                % Versions.ebean,
    "io.ebean"           % "ebean-ddl-generator"  % Versions.ebean,
    "io.ebean"           % "ebean-agent"          % Versions.ebeanAgent,
    "io.ebean"           % "querybean-agent"      % Versions.ebeanQueryBeanAgent,
    "io.ebean"           % "querybean-generator"  % Versions.ebeanQueryBeanGen % Test,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play              % Test,
    "com.typesafe.play" %% "filters-helpers"      % Versions.play              % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play              % Test,
    ("org.reflections"   % "reflections"          % "0.9.12")
      .exclude("com.google.code.findbugs", "annotations")
      .classifier("")
  )

  val plugin = libraryDependencies ++= Seq(
    "io.ebean"           % "ebean"                % Versions.ebean,
    "io.ebean"           % "ebean-agent"          % Versions.ebeanAgent,
    "io.ebean"           % "querybean-agent"      % Versions.ebeanQueryBeanAgent,
    "io.ebean"           % "querybean-generator"  % Versions.ebeanQueryBeanGen % Test,
    "com.typesafe"       % "config"               % Versions.typesafeConfig,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play              % Test,
    "com.typesafe.play" %% "filters-helpers"      % Versions.play              % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play              % Test,
    "org.clapper"       %% "classutil"            % "1.5.0"
  )
}
