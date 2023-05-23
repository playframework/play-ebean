lazy val plugins = (project in file(".")).dependsOn(sbtPlayEbean)

lazy val sbtPlayEbean = ProjectRef(Path.fileProperty("user.dir").getParentFile, "plugin")

resolvers ++= DefaultOptions.resolvers(snapshot = true)

addSbtPlugin("com.typesafe.play" % "play-docs-sbt-plugin" % sys.props.getOrElse("play.version", "2.9.0-M4"))

addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.8.0")

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.0")
