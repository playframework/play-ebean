package play.ebean.sbt

import java.net.URLClassLoader
import io.ebean.enhance.Transformer
import io.ebean.enhance.ant.OfflineFileTransform
import sbt.Keys._
import sbt.internal.inc.Hash
import sbt.internal.inc.LastModified
import sbt.internal.inc.Stamper
import sbt.AutoPlugin
import sbt.Compile
import sbt.Def
import sbt.Task
import sbt.inConfig
import sbt.settingKey
import sbt.taskKey
import xsbti.compile.CompileResult
import xsbti.compile.analysis.Stamp
import sbt._

import scala.util.control.NonFatal

object PlayEbean extends AutoPlugin {

  object autoImport {
    val playEbeanModels = taskKey[Seq[String]]("The packages that should be searched for ebean models to enhance.")
    val playEbeanVersion =
      settingKey[String]("The version of Play ebean that should be added to the library dependencies.")
    val playEbeanDebugLevel = settingKey[Int](
      "The debug level to use for the ebean agent. The higher, the more debug is output, with 9 being the most. -1 turns debugging off."
    )
    val playEbeanAgentArgs = taskKey[Map[String, String]]("The arguments to pass to the agent.")
  }

  import autoImport._

  override def trigger = noTrigger

  override def projectSettings = inConfig(Compile)(scopedSettings) ++ unscopedSettings

  def scopedSettings =
    Seq(
      playEbeanModels := configuredEbeanModels.value,
      manipulateBytecode := ebeanEnhance.value
    )

  def unscopedSettings =
    Seq(
      playEbeanDebugLevel := -1,
      playEbeanAgentArgs := Map("debug" -> playEbeanDebugLevel.value.toString),
      playEbeanVersion := readResourceProperty("play-ebean.version.properties", "play-ebean.version"),
      libraryDependencies ++=
        Seq(
          "com.typesafe.play" %% "play-ebean"   % playEbeanVersion.value,
          "org.glassfish.jaxb" % "jaxb-runtime" % "2.3.2"
        )
    )

  // This is replacement of old Stamp `Exists` representation
  private final val notPresent = "absent"

  def ebeanEnhance: Def.Initialize[Task[CompileResult]] =
    Def.task {

      val deps      = dependencyClasspath.value
      val classes   = classDirectory.value
      val result    = manipulateBytecode.value
      val agentArgs = playEbeanAgentArgs.value
      val analysis  = result.analysis.asInstanceOf[sbt.internal.inc.Analysis]

      val agentArgsString = agentArgs.map { case (key, value) => s"$key=$value" }.mkString(";")

      val originalContextClassLoader = Thread.currentThread.getContextClassLoader

      try {

        val classpath = deps.map(_.data.toURI.toURL).toArray :+ classes.toURI.toURL

        val classLoader = new java.net.URLClassLoader(classpath, null)

        Thread.currentThread.setContextClassLoader(classLoader)

        val transformer = new Transformer(classLoader, agentArgsString)

        val fileTransform = new OfflineFileTransform(transformer, classLoader, classes.getAbsolutePath)

        try {
          fileTransform.process(playEbeanModels.value.mkString(","))
        } catch {
          case NonFatal(_) =>
        }

      } finally {
        Thread.currentThread.setContextClassLoader(originalContextClassLoader)
      }

      val allProducts = analysis.relations.allProducts

      /**
       * Updates stamp of product (class file) by preserving the type of a passed stamp.
       * This way any stamp incremental compiler chooses to use to mark class files will
       * be supported.
       */
      def updateStampForClassFile(file: File, stamp: Stamp): Stamp =
        stamp match {
          case _: LastModified => Stamper.forLastModified(file)
          case _: Hash         => Stamper.forHash(file)
        }

      // Since we may have modified some of the products of the incremental compiler, that is, the compiled template
      // classes and compiled Java sources, we need to update their timestamps in the incremental compiler, otherwise
      // the incremental compiler will see that they've changed since it last compiled them, and recompile them.
      val updatedAnalysis = analysis.copy(stamps = allProducts.foldLeft(analysis.stamps) { (stamps, classFile) =>
        val existingStamp = stamps.product(classFile)
        if (existingStamp.writeStamp == notPresent) {
          throw new java.io.IOException(
            "Tried to update a stamp for class file that is not recorded as "
              + s"product of incremental compiler: $classFile"
          )
        }
        stamps.markProduct(classFile, updateStampForClassFile(classFile, existingStamp))
      })

      result.withAnalysis(updatedAnalysis)
    }

  private def configuredEbeanModels =
    Def.task {
      import java.util.{ List => JList }
      import java.util.{ Map => JMap }

      import collection.JavaConverters._

      // Creates a classloader with all the dependencies and all the resources, from there we can use the play ebean
      // code to load the config as it would be loaded in production
      def withClassLoader[T](block: ClassLoader => T): T = {
        val classpath =
          unmanagedResourceDirectories.value.map(_.toURI.toURL) ++ dependencyClasspath.value.map(_.data.toURI.toURL)
        val classLoader = new URLClassLoader(classpath.toArray, null)
        try {
          block(classLoader)
        } catch {
          case e: Exception =>
            // Since we're about to close the classloader, we can't risk any classloading that the thrown exception may
            // do when we later interogate it, so instead we create a new exception here, with the old exceptions message
            // and stack trace
            def clone(t: Throwable): RuntimeException = {
              val cloned = new RuntimeException(s"${t.getClass.getName}: ${t.getMessage}")
              cloned.setStackTrace(t.getStackTrace)
              if (t.getCause != null) {
                cloned.initCause(clone(t.getCause))
              }
              cloned
            }

            throw clone(e)
        } finally {
          classLoader.close()
        }
      }

      withClassLoader { classLoader =>
        val configLoader = classLoader
          .loadClass("play.db.ebean.ModelsConfigLoader")
          .asSubclass(classOf[java.util.function.Function[ClassLoader, JMap[String, JList[String]]]])
        val config = configLoader.getDeclaredConstructor().newInstance().apply(classLoader)

        if (config.isEmpty) {
          Seq("models.*")
        } else {
          config.asScala.flatMap(_._2.asScala).toSeq.distinct
        }
      }
    }

  private def readResourceProperty(resource: String, property: String): String = {
    val props  = new java.util.Properties
    val stream = getClass.getClassLoader.getResourceAsStream(resource)
    try {
      props.load(stream)
    } catch {
      case e: Exception =>
    } finally {
      if (stream ne null) stream.close()
    }
    props.getProperty(property)
  }
}
