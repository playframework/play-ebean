import sbt.inc.Analysis

val PlayVersion = playVersion(sys.props.getOrElse("play.version", "2.4.0"))

val PlayEnhancerVersion = "1.1.0"

lazy val root = project
  .in(file("."))
  .enablePlugins(PlayRootProject)
  .aggregate(core)
  .settings(
    name := "play-ebean-root"
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(Playdoc, PlayLibrary)
  .settings(
    name := "play-ebean",
    libraryDependencies ++= playEbeanDeps,
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "play/db/ebean/**"
    )
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
    resourceGenerators in Compile <+= generateVersionFile,
    scriptedLaunchOpts ++= Seq("-Dplay-ebean.version=" + version.value),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (publishLocal in core).value
    }
  )

playBuildRepoName in ThisBuild := "play-ebean"
playBuildExtraTests := {
  (scripted in plugin).toTask("").value
}
playBuildExtraPublish := {
  (PgpKeys.publishSigned in plugin).value
}

// Dependencies

def playEbeanDeps = Seq(
  "com.typesafe.play" %% "play-java-jdbc" % PlayVersion,
  "com.typesafe.play" %% "play-jdbc-evolutions" % PlayVersion,
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.7.2",
  avajeEbeanormAgent,
  "com.typesafe.play" %% "play-test" % PlayVersion % Test
)

def sbtPlayEbeanDeps = Seq(
  avajeEbeanormAgent,
  "com.typesafe" % "config" % "1.3.0"
)

def avajeEbeanormAgent = "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "4.5.3"

// Ebean enhancement

def enhanceEbeanClasses(classpath: Classpath, analysis: Analysis, classDirectory: File, pkg: String): Analysis = {
  // Ebean (really hacky sorry)
  val cp = classpath.map(_.data.toURI.toURL).toArray :+ classDirectory.toURI.toURL
  val cl = new java.net.URLClassLoader(cp)
  val t = cl.loadClass("com.avaje.ebean.enhance.agent.Transformer").getConstructor(classOf[Array[URL]], classOf[String]).newInstance(cp, "debug=0").asInstanceOf[AnyRef]
  val ft = cl.loadClass("com.avaje.ebean.enhance.ant.OfflineFileTransform").getConstructor(
    t.getClass, classOf[ClassLoader], classOf[String], classOf[String]
  ).newInstance(t, ClassLoader.getSystemClassLoader, classDirectory.getAbsolutePath, classDirectory.getAbsolutePath).asInstanceOf[AnyRef]
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

