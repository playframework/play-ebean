/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt.Keys.libraryDependencies

import sbt._

object Dependencies {

  object ScalaVersions {
    val scala212 = "2.12.19"
    val scala213 = "2.13.14"
    val scala3   = "3.3.3"
  }

  object Versions {
    val play: String   = "2.9.5"
    val ebean          = "15.5.2"
    val typesafeConfig = "1.4.3"
  }

  val ebean = libraryDependencies ++= Seq(
    ("io.ebean" % "ebean"               % Versions.ebean).excludeAll(ExclusionRule("com.fasterxml.jackson.core")),
    "io.ebean"  % "ebean-ddl-generator" % Versions.ebean,
    "io.ebean"  % "ebean-agent"         % Versions.ebean,
    "com.typesafe.play" %% "play-java-jdbc"       % Versions.play,
    "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
    "com.typesafe.play" %% "play-guice"           % Versions.play % Test,
    "com.typesafe.play" %% "play-filters-helpers" % Versions.play % Test,
    "com.typesafe.play" %% "play-test"            % Versions.play % Test,
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
