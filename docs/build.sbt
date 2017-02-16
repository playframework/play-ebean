SettingKey[Seq[File]]("migrationManualSources") := Nil

lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin, PlayEnhancer)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    libraryDependencies += component("play-java-forms"),
    libraryDependencies += component("play-test") % Test,
    libraryDependencies += "com.h2database" % "h2" % "1.4.193" % Test,
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    // No resource directories shuts the ebean agent up about java sources in the classes directory
    unmanagedResourceDirectories in Test := Nil,
    parallelExecution in Test := false,
    scalaVersion := "2.12.1"
  )
  .settings(PlayEbean.unscopedSettings: _*)
  .settings(inConfig(Test)(Seq(
    playEbeanModels := Seq("javaguide.ebean.*"),
    manipulateBytecode := PlayEbean.ebeanEnhance.value
  )): _*)
  .dependsOn(playEbean)

lazy val playEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "core")
