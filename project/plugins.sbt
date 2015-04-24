libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value

addSbtPlugin("com.typesafe.play" % "interplay" % "0.1.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8.5")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.2")

addSbtPlugin("com.eed3si9n" % "bintray-sbt" % "0.3.0-a1934a5457f882053b08cbdab5fd4eb3c2d1285d")
resolvers += Resolver.url("bintray-eed3si9n-sbt-plugins", url("https://dl.bintray.com/eed3si9n/sbt-plugins/"))(Resolver.ivyStylePatterns)
