resolvers ++= DefaultOptions.resolvers(snapshot = true)
addSbtPlugin("com.typesafe.play" % "sbt-play-ebean" % sys.props("project.version"))
