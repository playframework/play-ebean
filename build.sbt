import sbt.inc.Analysis
import interplay.ScalaVersions._

val Versions = new {
  val play: String = playVersion(sys.props.getOrElse("play.version", "2.6.9"))
  val playEnhancer = "1.2.2"
  val ebean = "11.7.1"
  val ebeanAgent = "11.5.1"
  val typesafeConfig = "1.3.2"
}

lazy val root = project
  .in(file("."))
  .enablePlugins(PlayRootProject, CrossPerProjectPlugin)
  .aggregate(core, plugin)
  .settings(
    name := "play-ebean-root",
    releaseCrossBuild := false
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(Playdoc, PlayLibrary, JacocoPlugin)
  .settings(
    name := "play-ebean",
    crossScalaVersions := Seq(scala211, scala212),
    libraryDependencies ++= playEbeanDeps,
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
  .enablePlugins(PlaySbtPlugin)
  .settings(
    name := "sbt-play-ebean",
    organization := "com.typesafe.sbt",
    libraryDependencies ++= sbtPlayEbeanDeps,

    libraryDependencies ++= Seq(
      sbtPluginDep("com.typesafe.sbt" % "sbt-play-enhancer" % Versions.playEnhancer, (sbtVersion in pluginCrossBuild).value, scalaVersion.value),
      sbtPluginDep("com.typesafe.play" % "sbt-plugin" % Versions.play, (sbtVersion in pluginCrossBuild).value, scalaVersion.value)
    ),

    resourceGenerators in Compile += generateVersionFile.taskValue,
    scriptedLaunchOpts ++= Seq("-Dplay-ebean.version=" + version.value),
    scriptedDependencies := {
      val () = publishLocal.value
    }
  )

playBuildRepoName in ThisBuild := "play-ebean"
// playBuildExtraTests := {
//  (scripted in plugin).toTask("").value
// }
playBuildExtraPublish := {
  (PgpKeys.publishSigned in plugin).value
}

// Dependencies
lazy val ebeanDeps = Seq(
  "io.ebean" % "ebean" % Versions.ebean,
  "io.ebean" % "ebean-agent" % Versions.ebeanAgent
)

lazy val playEbeanDeps = ebeanDeps ++ Seq(
  "com.typesafe.play" %% "play-java-jdbc" % Versions.play,
  "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
  "com.typesafe.play" %% "play-guice" % Versions.play % Test,
  "com.typesafe.play" %% "filters-helpers" % Versions.play % Test,
  "com.typesafe.play" %% "play-test" % Versions.play % Test
)

lazy val sbtPlayEbeanDeps = ebeanDeps ++ Seq(
  "com.typesafe" % "config" % Versions.typesafeConfig
)

// sbt deps
def sbtPluginDep(moduleId: ModuleID, sbtVersion: String, scalaVersion: String) = {
  Defaults.sbtPluginExtra(moduleId, CrossVersion.binarySbtVersion(sbtVersion), CrossVersion.binaryScalaVersion(scalaVersion))
}

// Ebean enhancement
def enhanceEbeanClasses(classpath: Classpath, analysis: Analysis, classDirectory: File, pkg: String): Analysis = {
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
