import sbt.Keys.libraryDependencies
import sbt._

object Dependencies {

  object ScalaVersions {
    val scala212 = "2.12.15"
    val scala213 = "2.13.8"
  }

  object Versions {
    val play: String   = "2.8.15"
    val ebean          = "13.4.0"
    val typesafeConfig = "1.4.2"
  }

  val ebean = libraryDependencies ++= Seq(
    "io.ebean"           % "ebean"                % Versions.ebean,
    "io.ebean"           % "ebean-ddl-generator"  % Versions.ebean,
    "io.ebean"           % "ebean-agent"          % Versions.ebean,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play % Test,
    "com.typesafe.play" %% "filters-helpers"      % Versions.play % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play % Test,
    ("org.reflections"   % "reflections"          % "0.9.12")
      .exclude("com.google.code.findbugs", "annotations")
      .classifier("")
  )

  val plugin = libraryDependencies ++= Seq(
    "io.ebean"           % "ebean"                % Versions.ebean,
    "io.ebean"           % "ebean-agent"          % Versions.ebean,
    "com.typesafe"       % "config"               % Versions.typesafeConfig,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play % Test,
    "com.typesafe.play" %% "filters-helpers"      % Versions.play % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play % Test
  )
}
