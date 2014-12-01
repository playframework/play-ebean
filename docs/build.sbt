import scala.util.Properties.isJavaAtLeast

lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies += component("play-test") % "test",
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    PlayDocsKeys.javaManualSourceDirectories ++= {
      if (isJavaAtLeast("1.8")) {
        (baseDirectory.value / "manual" / "working" / "javaGuide" ** "java8code").get
      } else Nil
    }
  )
  .settings(SbtEbean.unscopedSettings: _*)
  .settings(inConfig(Test)(Seq(
    EbeanKeys.models := Seq("javaguide.ebean.*"),
    compile <<= SbtEbean.ebeanEnhance
  )): _*)
