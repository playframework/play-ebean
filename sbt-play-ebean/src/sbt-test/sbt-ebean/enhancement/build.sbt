lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.12.1"

sourceDirectory in Test := baseDirectory.value / "tests"

scalaSource in Test := baseDirectory.value / "tests"

javaSource in Test := baseDirectory.value / "tests"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "1.4.193"

resolvers += "scalaz-releases" at "https://dl.bintray.com/scalaz/releases" // specs2 depends on scalaz-stream

PlayKeys.playOmnidoc := false