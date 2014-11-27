lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava)

sourceDirectory in Test := baseDirectory.value / "tests"

scalaSource in Test := baseDirectory.value / "tests"

javaSource in Test := baseDirectory.value / "tests"
