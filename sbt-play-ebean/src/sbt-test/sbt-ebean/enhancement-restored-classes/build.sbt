// Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>

lazy val root = project
  .in(file("."))
  .enablePlugins(PlayJava, PlayEbean)

scalaVersion := sys.props("scala.version")

resolvers ++= Seq(Resolver.sonatypeCentralSnapshots, Resolver.ApacheMavenSnapshotsRepo)

Test / sourceDirectory := baseDirectory.value / "tests"

Test / scalaSource := baseDirectory.value / "tests"

Test / javaSource := baseDirectory.value / "tests"

// The file in sbt 2's cache that sbt restored models/Task.class from, as a link
def cachedTaskRecord(state: State) = Project.extract(state).get(baseDirectory) / "cached-task-class"

commands ++= Seq(
  Command.command("recordCachedTask") { state =>
    val task = Project.extract(state).get(Compile / classDirectory).toPath.resolve("models/Task.class")
    if (java.nio.file.Files.isSymbolicLink(task)) {
      IO.write(cachedTaskRecord(state), task.toRealPath().toString)
      state.log.info(s"models/Task.class was restored from ${task.toRealPath()}")
    }
    state
  },
  // The names of the cached files contain the SHA-256 of their content
  Command.command("checkCachedTask") { state =>
    val record = cachedTaskRecord(state)
    if (record.exists) {
      val cached  = file(IO.read(record))
      val content = java.security.MessageDigest.getInstance("SHA-256").digest(IO.readBytes(cached))
      if (!cached.getName.contains(content.map("%02x".format(_)).mkString)) sys.error(s"$cached was changed")
      state.log.info(s"$cached is unchanged")
    }
    state
  },
)
