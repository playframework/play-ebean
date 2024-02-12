// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := sys.props("scala.version")

Test / sourceDirectory := baseDirectory.value / "tests"

Test / scalaSource := baseDirectory.value / "tests"

Test / javaSource := baseDirectory.value / "tests"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "2.2.224"
