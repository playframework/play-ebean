lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.6"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "1.4.196"

PlayKeys.playOmnidoc := false