// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

import Dependencies.ScalaVersions.scala212
import Dependencies.ScalaVersions.scala213
import Dependencies.ScalaVersions.scala3
import Dependencies.Versions
import com.typesafe.tools.mima.core._
import sbt.Append.appendSeq
import xsbti.compile.CompileAnalysis

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

val previousVersion: Option[String] = Some("7.0.0")

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet,
  mimaBinaryIssueFilters ++= Seq(
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, plugin)
  .disablePlugins(MimaPlugin)
  .settings(
    scalaVersion       := scala3,
    name               := "play-ebean-root",
    crossScalaVersions := Nil,
    publish / skip     := true,
  )
  .settings(
    (Compile / headerSources) ++=
      ((baseDirectory.value ** ("*.properties" || "*.md" || "*.sbt"))
        --- (baseDirectory.value ** "target" ** "*")
        --- (baseDirectory.value / ".github" ** "*")
        --- (baseDirectory.value / "docs" ** "*")
        --- (baseDirectory.value / "sbt-play-ebean" ** "*")).get ++
        (baseDirectory.value / "project" ** "*.scala" --- (baseDirectory.value ** "target" ** "*")).get
  )

lazy val core = project
  .in(file("play-ebean"))
  .settings(
    name               := "play-ebean",
    scalaVersion       := scala213,
    crossScalaVersions := Seq(scala213, scala3),
    Dependencies.ebean,
    mimaSettings,
    Compile / compile := enhanceEbeanClasses(
      (Compile / dependencyClasspath).value,
      (Compile / compile).value,
      (Compile / classDirectory).value,
      "play/db/ebean/**"
    ),
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(SbtPlugin)
  .settings(
    name         := "sbt-play-ebean",
    organization := "com.typesafe.play",
    Dependencies.plugin,
    addSbtPlugin("org.playframework" % "sbt-plugin" % Versions.play),
    scalaVersion          := scala212,
    crossScalaVersions    := Seq(scala212),
    mimaPreviousArtifacts := Set.empty,
    Compile / resourceGenerators += generateVersionFile.taskValue,
    scriptedLaunchOpts ++= Seq(
      s"-Dproject.version=${version.value}",
    ),
    scriptedBufferLog    := false,
    scriptedDependencies := ((): Unit),
  )
  .settings(
    (Compile / headerSources) ++=
      (sourceDirectory.value / "sbt-test" ** ("*.java" || "*.sbt")).get
  )

def sbtPluginDep(moduleId: ModuleID, sbtVersion: String, scalaVersion: String) = {
  Defaults.sbtPluginExtra(
    moduleId,
    CrossVersion.binarySbtVersion(sbtVersion),
    CrossVersion.binaryScalaVersion(scalaVersion)
  )
}

// Ebean enhancement
def enhanceEbeanClasses(
    classpath: Classpath,
    analysis: CompileAnalysis,
    classDirectory: File,
    pkg: String
): CompileAnalysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl
    .loadClass("io.ebean.enhance.Transformer")
    .getConstructor(classOf[ClassLoader], classOf[String])
    .newInstance(cl, "debug=0")
    .asInstanceOf[AnyRef]
  val ft = cl
    .loadClass("io.ebean.enhance.ant.OfflineFileTransform")
    .getConstructor(
      t.getClass,
      classOf[ClassLoader],
      classOf[String]
    )
    .newInstance(t, ClassLoader.getSystemClassLoader, classDirectory.getAbsolutePath)
    .asInstanceOf[AnyRef]
  ft.getClass.getDeclaredMethod("process", classOf[String]).invoke(ft, pkg)
  analysis
}

// Version file
def generateVersionFile =
  Def.task {
    val version = (core / Keys.version).value
    val file    = (Compile / resourceManaged).value / "play-ebean.version.properties"
    val content = s"play-ebean.version=$version"
    IO.write(file, content)
    Seq(file)
  }
