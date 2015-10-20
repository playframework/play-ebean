lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

PlayKeys.playOmnidoc := false