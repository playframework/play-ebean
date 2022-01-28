lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.15"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "1.4.200"

