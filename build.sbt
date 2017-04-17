import sbt.inc.Analysis

val PlayVersion = playVersion(sys.props.getOrElse("play.version", "2.5.6"))

val PlayEnhancerVersion = "1.1.0"

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
    addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % PlayEnhancerVersion),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % PlayVersion),
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
  "com.typesafe.play" %% "play-java-jdbc" % PlayVersion,
  "com.typesafe.play" %% "play-jdbc-evolutions" % PlayVersion,
  "io.ebean" % "ebean" % "10.2.1",
  ebeanAgent,
  "com.typesafe.play" %% "play-guice" % PlayVersion % Test,
  "com.typesafe.play" %% "play-test" % PlayVersion % Test
)

def sbtPlayEbeanDeps = Seq(
  ebeanAgent,
  "com.typesafe" % "config" % "1.3.1"
)

def ebeanAgent = "io.ebean" % "ebean-agent" % "10.1.2"

// Ebean enhancement

def enhanceEbeanClasses(classpath: Classpath, analysis: Analysis, classDirectory: File, pkg: String): Analysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl.loadClass("io.ebean.enhance.agent.Transformer").getConstructor(classOf[Array[URL]], classOf[String]).newInstance(cp, "debug=0").asInstanceOf[AnyRef]
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
