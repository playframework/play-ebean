import Dependencies.ScalaVersions.{scala212, scala213}
import Dependencies.Versions
import sbt.Append.appendSeq
import xsbti.compile.CompileAnalysis

lazy val root = project
  .in(file("."))
  .aggregate(core, plugin)
  .settings(
    name := "play-ebean-root",
    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(PublishLibrary)
  .settings(
    name := "play-ebean",
    crossScalaVersions := Seq(scala212, scala213),
    Dependencies.ebean,
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "play/db/ebean/**"
    ),
    jacocoReportSettings := JacocoReportSettings("Jacoco Coverage Report", None, JacocoThresholds(), Seq(JacocoReportFormats.XML), "utf-8")
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(SbtPlugin, PublishSbtPlugin)
  .settings(
    name := "sbt-play-ebean",
    organization := "com.typesafe.sbt",
    Dependencies.plugin,
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Versions.play),
    crossScalaVersions := Seq(scala212),
    resourceGenerators in Compile += generateVersionFile.taskValue
  )

def sbtPluginDep(moduleId: ModuleID, sbtVersion: String, scalaVersion: String) = {
  Defaults.sbtPluginExtra(moduleId, CrossVersion.binarySbtVersion(sbtVersion), CrossVersion.binaryScalaVersion(scalaVersion))
}

// Ebean enhancement
def enhanceEbeanClasses(classpath: Classpath, analysis: CompileAnalysis, classDirectory: File, pkg: String): CompileAnalysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl.loadClass("io.ebean.enhance.Transformer").getConstructor(classOf[ClassLoader], classOf[String]).newInstance(cl, "debug=0").asInstanceOf[AnyRef]
  val ft = cl.loadClass("io.ebean.enhance.ant.OfflineFileTransform").getConstructor(
    t.getClass, classOf[ClassLoader], classOf[String]
  ).newInstance(t, ClassLoader.getSystemClassLoader, classDirectory.getAbsolutePath).asInstanceOf[AnyRef]
  ft.getClass.getDeclaredMethod("process", classOf[String]).invoke(ft, pkg)
  analysis
}

// Version file
def generateVersionFile = Def.task {
  val version = (Keys.version in core).value
  val file = (resourceManaged in Compile).value / "play-ebean.version.properties"
  val content = s"play-ebean.version=$version"
  IO.write(file, content)
  Seq(file)
}
