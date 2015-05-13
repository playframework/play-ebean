lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, SbtEbean)

resolvers ++= DefaultOptions.resolvers(snapshot = true)

PlayKeys.playOmnidoc := false
