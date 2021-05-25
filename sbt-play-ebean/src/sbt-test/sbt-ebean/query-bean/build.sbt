lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

Compile / packageBin := (Compile / packageBin).dependsOn(Compile / playEbeanQueryBeanProcessAnnotation).value

scalaVersion := "2.13.5"

sourceDirectory in Test := baseDirectory.value / "tests"

scalaSource in Test := baseDirectory.value / "tests"

javaSource in Test := baseDirectory.value / "tests"

resolvers ++= DefaultOptions.resolvers(snapshot = true)

libraryDependencies += "com.h2database" % "h2" % "1.4.196"

resolvers += "scalaz-releases".at("https://dl.bintray.com/scalaz/releases") // specs2 depends on scalaz-stream
