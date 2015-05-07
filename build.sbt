import sbt.inc.Analysis

val PlayVersion = sys.props.getOrElse("play.version", "2.4.0-RC2")

lazy val root = project
  .in(file("."))
  .disablePlugins(BintrayPlugin)
  .aggregate(core)
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(noPublish: _*)
  .settings(
    name := "play-ebean-root"
  )

lazy val core = project
  .in(file("play-ebean"))
  .enablePlugins(Playdoc, Omnidoc)
  .disablePlugins(BintrayPlugin)
  .settings(common: _*)
  .settings(crossScala: _*)
  .settings(publishMavenCentral: _*)
  .settings(
    name := "play-ebean",
    libraryDependencies ++= playEbeanDeps,
    OmnidocKeys.githubRepo := "playframework/play-ebean",
    OmnidocKeys.tagPrefix := "",
    compile in Compile := enhanceEbeanClasses(
      (dependencyClasspath in Compile).value,
      (compile in Compile).value,
      (classDirectory in Compile).value,
      "play/db/ebean/**"
    ),
    (javacOptions in compile) := Seq("-source", "1.8", "-target", "1.8"),
    (javacOptions in doc) := Seq("-source", "1.8")
  )

lazy val plugin = project
  .in(file("sbt-play-ebean"))
  .settings(common: _*)
  .settings(scriptedSettings: _*)
  .settings(publishBintray: _*)
  .settings(
    name := "sbt-play-ebean",
    organization := "com.typesafe.sbt",
    sbtPlugin := true,
    libraryDependencies ++= sbtPlayEbeanDeps,
    addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.1.0-RC2"),
    (javacOptions in compile) := Seq("-source", "1.7", "-target", "1.7"),
    (javacOptions in doc) := Seq("-source", "1.7"),
    addSbtPlugin("com.typesafe.play" % "sbt-plugin" % PlayVersion),
    resourceGenerators in Compile <+= generateVersionFile,
    scriptedLaunchOpts ++= Seq("-Dplay-ebean.version=" + version.value),
    scriptedDependencies := {
      val () = publishLocal.value
      val () = (publishLocal in core).value
    }
  )

// Shared settings

def common: Seq[Setting[_]] = releaseCommonSettings ++ Seq(
  organization := "com.typesafe.play",
  scalaVersion := sys.props.get("scala.version").getOrElse("2.10.5"),
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  resolvers ++= DefaultOptions.resolvers(snapshot = true),
  resolvers += Resolver.typesafeRepo("releases"),
  homepage := Some(url("https://github.com/playframework/play-ebean")),
  licenses := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
  pomExtra := {
    <scm>
      <url>https://github.com/playframework/play-ebean</url>
      <connection>scm:git:git@github.com:playframework/play-ebean.git</connection>
    </scm>
      <developers>
        <developer>
          <id>playframework</id>
          <name>Play Framework Team</name>
          <url>https://github.com/playframework</url>
        </developer>
      </developers>
  },
  pomIncludeRepository := { _ => false }
)

// Release settings
def releaseCommonSettings: Seq[Setting[_]] = releaseSettings ++ {
  import sbtrelease._
  import ReleaseStateTransformations._
  import ReleaseKeys._
  import sbt.complete.Parser

  def inputTaskStep(key: InputKey[_], input: String) = ReleaseStep(action = { state =>
    val extracted = Project.extract(state)
    val inputTask = extracted.get(Scoped.scopedSetting(key.scope, key.key))
    val task = Parser.parse(input, inputTask.parser(state)) match {
      case Right(t) => t
      case Left(msg) => sys.error(s"Invalid programmatic input:\n$msg")
    }
    GlobalPlugin.evaluate(state, extracted.structure, task, key :: Nil)._1
  })

  def taskStep(key: TaskKey[_]) = ReleaseStep(action = { state =>
    Project.extract(state).runTask(key, state)._1
  })

  Seq(
    publishArtifactsAction := PgpKeys.publishSigned.value,
    tagName := (version in ThisBuild).value,

    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      inputTaskStep(scripted in plugin, ""),
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      taskStep(bintrayRelease in plugin),
      inputTaskStep(SonatypeKeys.sonatypeRelease in core, ""),
      pushChanges
    )
  )
}

def crossScala: Seq[Setting[_]] = Seq(
  crossScalaVersions := Seq("2.10.5", "2.11.6")
)

def publishMavenCentral: Seq[Setting[_]] = sonatypeSettings ++ Seq(
  SonatypeKeys.profileName := "com.typesafe"
)

def publishBintray: Seq[Setting[_]] = Seq(
  publishTo := {
    if (isSnapshot.value) Some(Opts.resolver.sonatypeSnapshots)
    else publishTo.value
  },
  publishMavenStyle := isSnapshot.value,
  bintrayOrganization := Some("playframework"),
  bintrayRepository := "sbt-plugin-releases",
  bintrayPackage := "sbt-play-ebean",
  bintrayReleaseOnPublish := false
)

def noPublish: Seq[Setting[_]] = Seq(
  publish := {},
  publishLocal := {},
  PgpKeys.publishSigned := {},
  publishTo := Some(Resolver.file("no-publish", crossTarget.value / "no-publish"))
)

// Dependencies

def playEbeanDeps = Seq(
  "com.typesafe.play" %% "play-java-jdbc" % PlayVersion,
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.5.6",
  avajeEbeanormAgent,
  "com.typesafe.play" %% "play-test" % PlayVersion % Test
)

def sbtPlayEbeanDeps = Seq(
  avajeEbeanormAgent,
  "com.typesafe" % "config" % "1.3.0-M3"
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

