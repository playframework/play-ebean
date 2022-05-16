import Dependencies.ScalaVersions.scala212

import Dependencies.ScalaVersions.scala213
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

lazy val mimaSettings = Seq(
  mimaPreviousArtifacts := Set(
    organization.value %% name.value % "6.0.0" // previousStableVersion.value
    // .getOrElse(throw new Error("Unable to determine previous version"))
  ),
  mimaBinaryIssueFilters ++= Seq(
    // https://github.com/playframework/play-ebean/pull/281 - Removed io.ebean.EbeanServer in Ebean 13.6.0
    ProblemFilters.exclude[IncompatibleMethTypeProblem]("play.db.ebean.EbeanDynamicEvolutions.generateEvolutionScript")
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, plugin)
  .disablePlugins(MimaPlugin)
  .settings(
    name                := "play-ebean-root",
    crossScalaVersions  := Nil,
    publish / skip      := true,
    sonatypeProfileName := "com.typesafe.play"
  )

lazy val core = project
  .in(file("play-ebean"))
  .settings(
    name               := "play-ebean",
    crossScalaVersions := Seq(scala212, scala213),
    Dependencies.ebean,
    mimaSettings,
    Compile / compile := enhanceEbeanClasses(
      (Compile / dependencyClasspath).value,
      (Compile / compile).value,
      (Compile / classDirectory).value,
      "play/db/ebean/**"
    ),
    sonatypeProfileName := "com.typesafe.play"
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(SbtPlugin)
  .disablePlugins(MimaPlugin)
  .settings(
    name         := "sbt-play-ebean",
    organization := "com.typesafe.play",
    Dependencies.plugin,
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Versions.play),
    crossScalaVersions := Seq(scala212),
    Compile / resourceGenerators += generateVersionFile.taskValue,
    scriptedLaunchOpts ++= Seq(
      s"-Dscala.version=${scalaVersion.value}",
      s"-Dscala.crossVersions=${(core / crossScalaVersions).value.mkString(",")}",
      s"-Dproject.version=${version.value}",
    ),
    scriptedBufferLog    := false,
    scriptedDependencies := ((): Unit),
    sonatypeProfileName  := "com.typesafe.play"
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

addCommandAlias(
  "validateCode",
  List(
    "headerCheckAll",
    "scalafmtSbtCheck",
    "scalafmtCheckAll",
  ).mkString(";")
)
