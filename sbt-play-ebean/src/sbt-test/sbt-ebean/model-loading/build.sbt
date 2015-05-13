lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

resolvers ++= DefaultOptions.resolvers(snapshot = true)

PlayKeys.playOmnidoc := false
