import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import Dependencies.ScalaVersions._
import de.heikoseeberger.sbtheader.HeaderPlugin

object Common extends AutoPlugin {

  import HeaderPlugin.autoImport._

  override def trigger = allRequirements

  override def requires = JvmPlugin && HeaderPlugin

  val repoName = "play-ebean"

  override def globalSettings =
    Seq(
      // organization
      organization := "com.typesafe.play",
      organizationName := "Lightbend Inc.",
      organizationHomepage := Some(url("https://www.lightbend.com/")),
      // scala settings
      scalaVersion := scala212,
      scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
      javacOptions ++= Seq("-encoding", "UTF-8"),
      // legal
      licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
      // on the web
      homepage := Some(url(s"https://github.com/playframework/${repoName}")),
      scmInfo := Some(
        ScmInfo(
          url(s"https://github.com/playframework/${repoName}"),
          s"scm:git:git@github.com:playframework/${repoName}.git"
        )
      ),
      developers += Developer(
        "contributors",
        "Contributors",
        "https://gitter.im/playframework/contributors",
        url("https://github.com/playframework")
      )
    )

  override def projectSettings =
    Seq(
      headerEmptyLine := false,
      headerLicense := Some(HeaderLicense.Custom("Copyright (C) Lightbend Inc. <https://www.lightbend.com>"))
    )
}
