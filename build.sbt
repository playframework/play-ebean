import sbt.inc.Analysis

val Versions = new {
  val play = playVersion(sys.props.getOrElse("play.version", "2.5.15"))
  val playEnhancer = "1.1.0"
  val ebean = "10.3.1"
  val ebeanAgent = "10.2.1"
  val typesafeConfig = "1.3.1"
}


lazy val root = project
  .in(file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(core)
  .settings(
    name := "play-ebean-root",
    releaseCrossBuild := false
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(Playdoc, PlayLibrary)
  .settings(jacoco.settings: _*)
  .settings(
    name := "play-ebean",
    libraryDependencies ++= playEbeanDeps,
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "play/db/ebean/**"
    ),
    jacoco.reportFormats in jacoco.Config := Seq(de.johoop.jacoco4sbt.XMLReport(encoding = "utf-8"))
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .enablePlugins(PlaySbtPlugin)
  .settings(
    name := "sbt-play-ebean",
    organization := "com.typesafe.sbt",
    libraryDependencies ++= sbtPlayEbeanDeps,
    addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % Versions.playEnhancer),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % Versions.play),
    resourceGenerators in Compile += generateVersionFile.taskValue,
    scriptedLaunchOpts ++= Seq("-Dplay-ebean.version=" + version.value),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (publishLocal in core).value
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

def playEbeanDeps = Seq(
  "com.typesafe.play" %% "play-java-jdbc" % Versions.play,
  "com.typesafe.play" %% "play-jdbc-evolutions" % Versions.play,
  "io.ebean" % "ebean" % Versions.ebean,
  ebeanAgent,
  "com.typesafe.play" %% "play-test" % Versions.play % Test
)

def sbtPlayEbeanDeps = Seq(
  ebeanAgent,
  "com.typesafe" % "config" % Versions.typesafeConfig
)

def ebeanAgent = "io.ebean" % "ebean-agent" % Versions.ebeanAgent

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
