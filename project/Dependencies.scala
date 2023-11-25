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
    val play: String   = "2.9.0"
    val ebean          = "13.17.3"
    val ebeanJakarta   = s"$ebean-jakarta"
    val typesafeConfig = "1.4.3"
  }

  val ebean = libraryDependencies ++= Seq(
    "io.ebean"           % "ebean"                % Versions.ebeanJakarta,
    "io.ebean"           % "ebean-ddl-generator"  % Versions.ebeanJakarta,
    "io.ebean"           % "ebean-agent"          % Versions.ebean,
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
    "io.ebean"     % "ebean"       % Versions.ebeanJakarta,
    "io.ebean"     % "ebean-agent" % Versions.ebean,
    "com.typesafe" % "config"      % Versions.typesafeConfig,
  )
}
