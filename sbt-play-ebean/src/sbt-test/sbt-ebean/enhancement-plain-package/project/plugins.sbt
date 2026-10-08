// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

resolvers ++= DefaultOptions.resolvers(snapshot = true)
addSbtPlugin("org.playframework" % "sbt-play-ebean" % sys.props("project.version"))
