lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.13.10"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "2.1.212"
