import sbt._
import Keys._

class Publish(isLibrary: Boolean, repoName: String) extends AutoPlugin {

  import bintray.BintrayPlugin
  import bintray.BintrayPlugin.autoImport._

  override def trigger  = noTrigger
  override def requires = BintrayPlugin

  val (releaseRepo, snapshotRepo) =
    if (isLibrary)
      ("maven", "snapshots")
    else
      ("sbt-plugin-releases", "sbt-plugin-snapshots")

  override def projectSettings =
    Seq(
      bintrayOrganization := Some("funraise"),
      bintrayRepository := (if (isSnapshot.value) snapshotRepo else releaseRepo),
      bintrayPackage := repoName,
      // maven style should only be used for libraries, not for plugins
      publishMavenStyle := isLibrary,
      bintrayPackageLabels := Seq("playframework", "play-ebean", "plugin")
    )
}

object PublishLibrary   extends Publish(isLibrary = true, repoName = "play-ebean")
object PublishSbtPlugin extends Publish(isLibrary = false, repoName = "sbt-play-ebean")
