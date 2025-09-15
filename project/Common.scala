/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
 */

import sbtheader.HeaderPlugin.autoImport.HeaderPattern.commentBetween
import sbt.Keys.*
import sbt.*
import sbt.plugins.JvmPlugin
import sbtheader.CommentStyle
import sbtheader.FileType
import sbtheader.HeaderPlugin
import sbtheader.LineCommentCreator

object Common extends AutoPlugin {

  import HeaderPlugin.autoImport._

  override def trigger = allRequirements

  override def requires = JvmPlugin && HeaderPlugin

  val repoName = "play-ebean"

  override def globalSettings: Seq[Setting[_]] =
    Seq(
      // organization
      organization         := "org.playframework",
      organizationName     := "The Play Framework Project",
      organizationHomepage := Some(url("https://playframework.com/")),
      scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
      javacOptions ++= Seq("-encoding", "UTF-8"),
      // legal
      licenses := Seq("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")),
      // on the web
      homepage := Some(url(s"https://github.com/playframework/$repoName")),
      scmInfo  := Some(
        ScmInfo(
          url(s"https://github.com/playframework/$repoName"),
          s"scm:git:git@github.com:playframework/$repoName.git"
        )
      ),
      developers += Developer(
        "playframework",
        "The Play Framework Contributors",
        "contact@playframework.com",
        url("https://github.com/playframework")
      )
    )

  override def projectSettings: Seq[Def.Setting[_]] =
    Seq(
      headerLicense := Some(
        HeaderLicense.Custom(
          "Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>"
        )
      ),
      headerMappings ++= Map(
        FileType("sbt")        -> HeaderCommentStyle.cppStyleLineComment,
        FileType("properties") -> HeaderCommentStyle.hashLineComment,
        FileType("md") -> CommentStyle(new LineCommentCreator("<!---", "-->"), commentBetween("<!---", "*", "-->"))
      )
    )
}
