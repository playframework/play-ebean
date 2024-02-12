// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

//#add-sbt-plugin
addSbtPlugin("org.playframework" % "sbt-play-ebean" % "8.0.0")
//#add-sbt-plugin

//#enable-plugin
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)

//#enable-plugin

//#play-ebean-models
Compile / playEbeanModels := Seq("models.*")
//#play-ebean-models

//#play-ebean-debug
playEbeanDebugLevel := 4
//#play-ebean-debug

//#play-ebean-agent-args
playEbeanAgentArgs += ("detect" -> "false")
//#play-ebean-agent-args

//#play-ebean-test
inConfig(Test)(PlayEbean.scopedSettings)

Test / playEbeanModels := Seq("models.*")
//#play-ebean-test
