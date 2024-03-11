/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt.Keys.libraryDependencies

import sbt._

object Dependencies {

  object ScalaVersions {
    val scala212 = "2.12.18"
    val scala213 = "2.13.12"
    val scala3   = "3.3.1"
  }

  object Versions {
<<<<<<< HEAD
    val play: String   = "2.9.1"
    val ebean          = "13.23.0"
    val ebeanJakarta   = s"$ebean-jakarta"
=======
    val play: String   = "3.0.1"
    val ebean          = "15.0.1"
>>>>>>> 5aa0fb6 (Ebean 15.0.1)
    val typesafeConfig = "1.4.3"
  }

  val ebean = libraryDependencies ++= Seq(
<<<<<<< HEAD
    "io.ebean"           % "ebean"                % Versions.ebean,
    "io.ebean"           % "ebean-ddl-generator"  % Versions.ebean,
    "io.ebean"           % "ebean-agent"          % Versions.ebean,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play % Test,
    "com.typesafe.play" %% "play-filters-helpers" % Versions.play % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play % Test,
=======
    ("io.ebean" % "ebean"               % Versions.ebean).excludeAll(ExclusionRule("com.fasterxml.jackson.core")),
    "io.ebean"  % "ebean-ddl-generator" % Versions.ebean,
    "io.ebean"  % "ebean-agent"         % Versions.ebean,
    "org.playframework" %% "play-java-jdbc"       % Versions.play,
    "org.playframework" %% "play-jdbc-evolutions" % Versions.play,
    "org.playframework" %% "play-guice"           % Versions.play % Test,
    "org.playframework" %% "play-filters-helpers" % Versions.play % Test,
    "org.playframework" %% "play-test"            % Versions.play % Test,
>>>>>>> 5a7fbec (Exclude jackson core dependencies from ebean and use Play's)
    ("org.reflections"   % "reflections"          % "0.10.2")
      .exclude("com.google.code.findbugs", "annotations")
      .classifier("")
  )

  val plugin = libraryDependencies ++= Seq(
    "io.ebean"     % "ebean"       % Versions.ebean,
    "io.ebean"     % "ebean-agent" % Versions.ebean,
    "com.typesafe" % "config"      % Versions.typesafeConfig,
  )
}
