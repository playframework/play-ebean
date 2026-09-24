// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

import Dependencies.ScalaVersions.publishedScalaVersions
import Dependencies.ScalaVersions.resolveScalaVersion
import Dependencies.ScalaVersions.scala212Version
import Dependencies.ScalaVersions.scala213Version
import Dependencies.ScalaVersions.scala3PluginVersion
import Dependencies.ScalaVersions.scala33LTSVersion
import Dependencies.Versions
import com.typesafe.tools.mima.core._
import sbt.Append.appendSeq
import xsbti.compile.CompileAnalysis

// Customise sbt-dynver's behaviour to make it work with tags which aren't v-prefixed
ThisBuild / dynverVTagPrefix := false
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots

// Sanity-check: assert that version comes from a tag (e.g. not a too-shallow clone)
// https://github.com/dwijnand/sbt-dynver/#sanity-checking-the-version
Global / onLoad := (Global / onLoad).value.andThen { s =>
  dynverAssertTagVersion.value
  s
}

val previousVersion: Option[String] = Some("8.3.0")

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := previousVersion.map(organization.value %% moduleName.value % _).toSet,
  mimaBinaryIssueFilters ++= Seq(
    ProblemFilters.exclude[MissingTypesProblem]("play.db.ebean.DefaultEbeanConfig$EbeanConfigParser"),
    // Play's Java Module superclass provides the inherited Scala bindings method and its Seq bridge.
    ProblemFilters.exclude[DirectAbstractMethodProblem]("play.api.inject.Module.bindings"),
  )
)

ThisBuild / javafmtFormatterCompatibleJavaVersion := 17

lazy val root = project
  .in(file("."))
  .aggregate(core, plugin)
  .disablePlugins(MimaPlugin)
  .settings(
    scalaVersion       := resolveScalaVersion(sys.props.getOrElse("scala.version", scala213Version)),
    name               := "play-ebean-root",
    crossScalaVersions := Nil,
    publish / skip     := true,
  )
  .settings(
    (Compile / headerSources) ++= Def.uncached(
      ((baseDirectory.value ** ("*.properties" || "*.md" || "*.sbt"))
        --- (baseDirectory.value ** "target" ** "*")
        --- (baseDirectory.value / ".github" ** "*")
        --- (baseDirectory.value / "docs" ** "*")
        --- (baseDirectory.value / "sbt-play-ebean" ** "*")).get() ++
        (baseDirectory.value / "project" ** "*.scala" --- (baseDirectory.value ** "target" ** "*")).get()
    ),
  )

lazy val core = project
  .in(file("play-ebean"))
  .settings(
    name               := "play-ebean",
    scalaVersion       := resolveScalaVersion(sys.props.getOrElse("scala.version", scala213Version)),
    crossScalaVersions := publishedScalaVersions,
    Dependencies.ebean,
    mimaSettings,
    Compile / compile := Def.uncached(
      enhanceEbeanClasses(
        (Compile / dependencyClasspath).value,
        (Compile / compile).value,
        (Compile / classDirectory).value,
        "play/db/ebean/**",
        fileConverter.value,
      )
    ),
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(SbtPlugin)
  .settings(
    name         := "sbt-play-ebean",
    organization := "org.playframework",
    Dependencies.plugin,
    addSbtPlugin("org.playframework" % "sbt-plugin" % Versions.play),
    scalaVersion                  := scala3PluginVersion,
    crossScalaVersions            := Seq(scala212Version, scala3PluginVersion),
    pluginCrossBuild / sbtVersion := {
      scalaBinaryVersion.value match {
        case "2.12" => "1.12.9"
        case _      => "2.1.0-M2"
      }
    },
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) => Seq("-Xsource:3")
        case _            => Seq.empty
      }
    },
    mimaPreviousArtifacts := Set.empty,
    Compile / resourceGenerators += generateVersionFile.taskValue,
    // Use the current lane-based app Scala as a fallback for local runs without scripted.scala.version.
    scriptedLaunchOpts ++= Seq(
      s"-Dproject.version=${version.value}",
      s"-Dscala.version=${resolveScriptedScala(
          sys.props.getOrElse(
            "scripted.scala.version",
            if (scalaBinaryVersion.value == "2.12") scala213Version else scala33LTSVersion
          )
        )}",
    ),
    scriptedBufferLog    := false,
    scriptedDependencies := publishLocal.value,
  )
  .settings(
    (Compile / headerSources) ++= Def.uncached(
      (sourceDirectory.value / "sbt-test" ** ("*.java" || "*.sbt")).get()
    ),
  )

def resolveScriptedScala(version: String): String =
  version match {
    case "scala212" | "2.12.x" => scala212Version
    case "scala213" | "2.13.x" => scala213Version
    case "scala3" | "3.x"      => scala33LTSVersion
    case selector              => resolveScalaVersion(selector)
  }

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
    pkg: String,
    converter: FileConverter,
): CompileAnalysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data).map(converter.toPath).map(_.toFile.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t  = cl
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
