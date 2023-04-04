import sbt.Keys.libraryDependencies

import sbt._
import scala.util.Properties

object Dependencies {

  object ScalaVersions {
    val scala212 = "2.12.17"
    val scala213 = "2.13.10"
    val scala33  = "3.3.0-RC3"
  }

  object Versions {
    val play: String   = "2.9.0-M3-SNAPSHOT"
    val ebean          = "13.15.2"
    val typesafeConfig = "1.4.2"
  }

  val ebean = libraryDependencies ++= {
    CrossVersion.partialVersion(Properties.versionNumberString) match {
        case Some((3, n)) => Seq(
          "io.ebean"           % "ebean"                % Versions.ebean,
          "io.ebean"           % "ebean-ddl-generator"  % Versions.ebean,
          "io.ebean"           % "ebean-agent"          % Versions.ebean,
          ("com.typesafe.play" %% "play-java-jdbc"       % Versions.play).cross(CrossVersion.for3Use2_13),
          ("com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play).cross(CrossVersion.for3Use2_13),
          "com.typesafe.play" %% "play-guice"           % Versions.play % Test,
          "com.typesafe.play" %% "filters-helpers"      % Versions.play % Test,
          "com.typesafe.play" %% "play-test"            % Versions.play % Test,
          ("org.reflections"   % "reflections"          % "0.10.2")
            .exclude("com.google.code.findbugs", "annotations")
            .classifier("")
        )
        case _          => Seq(
          "io.ebean"           % "ebean"                % Versions.ebean,
          "io.ebean"           % "ebean-ddl-generator"  % Versions.ebean,
          "io.ebean"           % "ebean-agent"          % Versions.ebean,
          ("com.typesafe.play" %% "play-java-jdbc"       % Versions.play),
          ("com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play),
          ("com.typesafe.play" %% "play-guice"           % Versions.play % Test),
          ("com.typesafe.play" %% "filters-helpers"      % Versions.play % Test),
          ("com.typesafe.play" %% "play-test"            % Versions.play % Test),
          ("org.reflections"   % "reflections"          % "0.10.2")
            .exclude("com.google.code.findbugs", "annotations")
            .classifier("")
        )
    }
  }
  val plugin = libraryDependencies ++= Seq(
    "io.ebean"     % "ebean"       % Versions.ebean,
    "io.ebean"     % "ebean-agent" % Versions.ebean,
    "com.typesafe" % "config"      % Versions.typesafeConfig,
  )
}
