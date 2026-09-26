// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

import sbtheader.CommentStyle
import sbtheader.FileType
import sbtheader.HeaderPlugin
import sbtheader.HeaderPlugin.autoImport.HeaderPattern.commentBetween
import sbtheader.LineCommentCreator

SettingKey[Seq[File]]("migrationManualSources") := Nil

ThisBuild / javafmtFormatterCompatibleJavaVersion := 17
ThisBuild / resolvers += Resolver.sonatypeCentralSnapshots

val scalaVersionAliases = Map(
  "2.13.x" -> "2.13.18",
  "3.3.x"  -> "3.3.8",
  "3.9.x"  -> "3.9.0",
  "3.next" -> "3.10.0-RC2",
)

lazy val docs = project
  .in(file("."))
  .enablePlugins(PlayDocsPlugin, PlayEbean)
  .settings(
    // use special snapshot play version for now
    libraryDependencies += component("play-java-forms"),
    libraryDependencies += component("play-test") % Test,
    libraryDependencies += "com.h2database"       % "h2" % "2.5.252" % Test,
    PlayDocsKeys.javaManualSourceDirectories     := (baseDirectory.value / "manual" / "working" / "javaGuide" ** "code")
      .get(),
    // No resource directories shuts the ebean agent up about java sources in the classes directory
    Test / unmanagedResourceDirectories := Nil,
    Test / parallelExecution            := false,
    scalaVersion                        := {
      val selected = sys.props.getOrElse("scala.version", "2.13.18")
      scalaVersionAliases.getOrElse(selected, selected)
    },
    crossScalaVersions := Seq("2.13.18", "3.3.8"),
    scalacOptions ++= {
      if (scalaVersion.value.startsWith("3.3.")) Seq("-release:17", "-Yfuture-lazy-vals") else Seq.empty
    },
  )
  .settings(
    Test / javafmt / sourceDirectories ++= (Test / unmanagedSourceDirectories).value,
  )
  .settings(
    headerLicense := Some(
      HeaderLicense.Custom(
        "Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>"
      )
    ),
    headerMappings ++= Map(
      FileType("sbt")        -> HeaderCommentStyle.cppStyleLineComment,
      FileType("properties") -> HeaderCommentStyle.hashLineComment,
      FileType("md") -> CommentStyle(new LineCommentCreator("<!---", "-->"), commentBetween("<!---", "*", "-->"))
    ),
    Compile / headerSources ++= Def.uncached(
      ((baseDirectory.value ** ("*.properties" || "*.md" || "*.sbt"))
        --- (baseDirectory.value ** "target" ** "*")).get()
    ),
  )
  .settings(PlayEbean.unscopedSettings)
  .settings(
    inConfig(Test)(
      Seq(
        playEbeanModels := Seq("javaguide.ebean.*")
      )
    )
  )
  .dependsOn(playEbean)

lazy val playEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "core")

val ignore = sys.props += ("sbt_validateCode" -> List(
  "evaluateSbtFiles",
  "validateDocs",
).mkString(";"))
