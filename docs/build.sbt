SettingKey[Seq[File]]("migrationManualSources") := Nil

lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin, PlayEbean)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    libraryDependencies += component("play-java-forms"),
    libraryDependencies += component("play-test") % Test,
    libraryDependencies += "com.h2database"       % "h2" % "2.1.214" % Test,
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    // No resource directories shuts the ebean agent up about java sources in the classes directory
    Test / unmanagedResourceDirectories := Nil,
    Test / parallelExecution            := false,
    scalaVersion                        := "2.13.10",
    crossScalaVersions                  := Seq("2.13.10", "3.3.0-RC5"),
  )
  .settings(PlayEbean.unscopedSettings: _*)
  .settings(
    inConfig(Test)(
      Seq(
        playEbeanModels := Seq("javaguide.ebean.*")
      )
    ): _*
  )
  .dependsOn(playEbean)

lazy val playEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "core")
