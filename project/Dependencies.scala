/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbt.Keys.libraryDependencies

import sbt._

object Dependencies {

  object ScalaVersions {
    val scala212Version   = "2.12.21"
    val scala213Version   = "2.13.18"
    val scala33LTSVersion = "3.3.8"
    val scala39LTSVersion = "3.9.0"
    val scala3NextVersion = "3.10.0-RC2"

    val publishedScalaVersions = Seq(scala213Version, scala33LTSVersion)

    private val scalaVersionAliases = Map(
      "2.12.x" -> scala212Version,
      "2.13.x" -> scala213Version,
      "3.3.x"  -> scala33LTSVersion,
      "3.9.x"  -> scala39LTSVersion,
      "3.next" -> scala3NextVersion,
    )

    def resolveScalaVersion(version: String): String = scalaVersionAliases.getOrElse(version, version)

    // sbt 2.1.0-M2 itself is built with Scala 3.9.0.
    val scala3PluginVersion = scala39LTSVersion
  }

  object Versions {
    val play: String   = "3.1.0-M10-e1f3c2a9-SNAPSHOT"
    val ebean          = "19.5.0"
    val typesafeConfig = "1.4.9"
  }

  val ebean = libraryDependencies ++= Seq(
    ("io.ebean" % "ebean"               % Versions.ebean).excludeAll(ExclusionRule("com.fasterxml.jackson.core")),
    "io.ebean"  % "ebean-ddl-generator" % Versions.ebean,
    "io.ebean"  % "ebean-agent"         % Versions.ebean,
    "org.playframework" %% "play-java-jdbc"       % Versions.play,
    "org.playframework" %% "play-jdbc-evolutions" % Versions.play,
    "org.playframework" %% "play-guice"           % Versions.play % Test,
    "org.playframework" %% "play-filters-helpers" % Versions.play % Test,
    "org.playframework" %% "play-test"            % Versions.play % Test,
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
