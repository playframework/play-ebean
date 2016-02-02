SettingKey[Seq[File]]("migrationManualSources") := Nil

lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin, PlayEnhancer)
  .settings(
    // use special snapshot play version for now
    resolvers ++= DefaultOptions.resolvers(snapshot = true),
    libraryDependencies += component("play-test") % Test,
    PlayDocsKeys.javaManualSourceDirectories := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code").get,
    // No resource directories shuts the ebean agent up about java sources in the classes directory
    unmanagedResourceDirectories in Test := Nil,
    parallelExecution in Test := false,
    scalaVersion := "2.11.7"
  )
  .settings(PlayEbean.unscopedSettings: _*)
  .settings(inConfig(Test)(Seq(
    playEbeanQueryGenerate := false,
    playEbeanQueryEnhance := false,
    playEbeanQueryDestDirectory := "app",
    playEbeanQueryResourceDirectory := "conf",
    playEbeanQueryModelsPackage := "models",
    playEbeanQueryModelsQueryModificationPackage := Set("models/query"),
    playEbeanQueryGenerateFinder := true,
    playEbeanQueryGenerateFinderField := true,
    playEbeanQueryGeneratePublicWhereField := true,
    playEbeanQueryGenerateAopStyle := true,
    playEbeanQueryArgs := "",
    playEbeanQueryProcessPackages := None,
    playEbeanModels := Seq("javaguide.ebean.*"),
    manipulateBytecode <<= PlayEbean.ebeanEnhance
  )): _*)
  .dependsOn(playEbean)

lazy val playEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "core")
