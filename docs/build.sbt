lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin, PlayEnhancer)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    resolvers += Resolver.typesafeRepo("releases"),
    libraryDependencies += component("play-test") % Test,
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    // No resource directories shuts the ebean agent up about java sources in the classes directory
    unmanagedResourceDirectories in Test := Nil,
    parallelExecution in Test := false
  )
  .settings(SbtEbean.unscopedSettings: _*)
  .settings(inConfig(Test)(Seq(
    EbeanKeys.models := Seq("javaguide.ebean.*"),
    manipulateBytecode <<= SbtEbean.ebeanEnhance
  )): _*)
  .dependsOn(playEbean)

lazy val playEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "core")