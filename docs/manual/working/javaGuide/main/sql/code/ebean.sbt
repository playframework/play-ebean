//#add-sbt-plugin
addSbtPlugin("com.typesafe.sbt" % "sbt-play-ebean" % "6.1.0")
//#add-sbt-plugin

//#enable-plugin
lazy val myProject = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)

//#enable-plugin

//#play-ebean-models
playEbeanModels in Compile := Seq("models.*")
//#play-ebean-models

//#play-ebean-debug
playEbeanDebugLevel := 4
//#play-ebean-debug

//#play-ebean-agent-args
playEbeanAgentArgs += ("detect" -> "false")
//#play-ebean-agent-args

//#play-ebean-test
inConfig(Test)(PlayEbean.scopedSettings)

playEbeanModels in Test := Seq("models.*")
//#play-ebean-test
