package play.ebean.sbt

import java.net.URLClassLoader

import com.typesafe.play.sbt.enhancer.PlayEnhancer
import org.avaje.ebean.typequery.generator.{Generator, GeneratorConfig}
import sbt.Keys._
import sbt._
import sbt.inc._
import scala.collection.JavaConverters._

import scala.util.control.NonFatal

object PlayEbean extends AutoPlugin {

  object autoImport {
    val playEbeanModels = taskKey[Seq[String]]("The packages that should be searched for ebean models to enhance.")
    val playEbeanVersion = settingKey[String]("The version of Play ebean that should be added to the library dependencies.")
    val playEbeanDebugLevel = settingKey[Int]("The debug level to use for the ebean agent. The higher, the more debug is output, with 9 being the most. -1 turns debugging off.")
    val playEbeanAgentArgs = taskKey[Map[String, String]]("The arguments to pass to the agent.")

    val playEbeanQueryGenerate = settingKey[Boolean]("Generate Query Beans from model classes. Default false.")
    val playEbeanQueryEnhance = settingKey[Boolean]("Enhance Query Beans from model classes. Defaults to false")
    val playEbeanQueryDestDirectory = settingKey[String]("Target directory for generated classes. Defaults to app ")
    val playEbeanQueryResourceDirectory = settingKey[String]("Resource directory to read configuration. Defaults to conf")
    val playEbeanQueryModelsPackage = settingKey[String]("Directory of models to scan to build query beans")
    val playEbeanQueryModelsQueryModificationPackage = settingKey[Set[String]]("Directories of matching query objects to rewrite field access to use getters. Defaults to [model/query]")
    val playEbeanQueryGenerateFinder = settingKey[Boolean]("Generate finder objects")
    val playEbeanQueryGenerateFinderField = settingKey[Boolean]("Modify models to add finder field")
    val playEbeanQueryGeneratePublicWhereField = settingKey[Boolean]("Public finder field")
    val playEbeanQueryGenerateAopStyle = settingKey[Boolean]("Use AOP style generation. Default true")
    val playEbeanQueryArgs = settingKey[String]("Args for generation, useful for logging / debugging generation ")
    val playEbeanQueryProcessPackages = settingKey[Option[String]]("Change to alter the initial package for scanning for model classes. By default views all")
  }

  import autoImport._

  // Must require PlayEnhancer to make sure it runs before we do
  override def requires = PlayEnhancer

  override def trigger = noTrigger

  override def projectSettings = {
    val querySettings = Seq(
      playEbeanQueryGenerate := false,
      playEbeanQueryEnhance := false,
      playEbeanQueryDestDirectory := "app",
      playEbeanQueryResourceDirectory := "conf",
      playEbeanQueryModelsPackage := "models",
      playEbeanQueryModelsQueryModificationPackage := Set("models/query"),
      playEbeanQueryGenerateFinder := true,
      playEbeanQueryGenerateFinderField := true,
      playEbeanQueryGeneratePublicWhereField := true,
      playEbeanQueryGenerateAopStyle := true,
      playEbeanQueryArgs := "",
      playEbeanQueryProcessPackages := None
    )

    inConfig(Compile)(scopedSettings) ++ unscopedSettings ++ querySettings
  }

  def scopedSettings = Seq(
    playEbeanModels <<= configuredEbeanModels,
    manipulateBytecode <<= ebeanEnhance
  )

  def unscopedSettings = Seq(
    playEbeanDebugLevel := -1,
    playEbeanAgentArgs := Map("debug" -> playEbeanDebugLevel.value.toString),
    playEbeanVersion := readResourceProperty("play-ebean.version.properties", "play-ebean.version"),
    libraryDependencies += "com.typesafe.play" %% "play-ebean" % playEbeanVersion.value
  )

  def ebeanEnhance: Def.Initialize[Task[Compiler.CompileResult]] = Def.task {

    val deps = dependencyClasspath.value
    val classes = classDirectory.value
    val result = manipulateBytecode.value
    val agentArgs = playEbeanAgentArgs.value
    val analysis = result.analysis

    val agentArgsString = agentArgs map { case (key, value) => s"$key=$value" } mkString ";"

    val originalContextClassLoader = Thread.currentThread.getContextClassLoader

    try {

      val classpath = deps.map(_.data.toURI.toURL).toArray :+ classes.toURI.toURL

      val classLoader = new java.net.URLClassLoader(classpath, null)

      Thread.currentThread.setContextClassLoader(classLoader)

      import com.avaje.ebean.enhance.agent._
      import com.avaje.ebean.enhance.ant._
      try {
        if(playEbeanQueryGenerate.value) {
          val config = new GeneratorConfig()
          config.setClassesDirectory(classes.getAbsolutePath)
          config.setDestDirectory(playEbeanQueryDestDirectory.value)
          config.setDestResourceDirectory(playEbeanQueryResourceDirectory.value)

          config.setEntityBeanPackage(playEbeanQueryModelsPackage.value)
          config.setAddFinderWherePublic(playEbeanQueryGeneratePublicWhereField.value)
          config.setAopStyle(playEbeanQueryGenerateAopStyle.value)

          val generator: Generator = new Generator(config)
          generator.generateQueryBeans()
          if (playEbeanQueryGenerateFinder.value) {
            generator.generateFinders()
          }
          if (playEbeanQueryGenerateFinderField.value) {
            generator.modifyEntityBeansAddFinderField()
          }
        }

        val transformer = new Transformer(classpath, agentArgsString)
        val fileTransform = new OfflineFileTransform(transformer, classLoader, classes.getAbsolutePath)
        fileTransform.process(playEbeanModels.value.mkString(","))
        if(playEbeanQueryEnhance.value) {
          val queryTransform = new org.avaje.ebean.typequery.agent.Transformer(playEbeanQueryArgs.value, classLoader, playEbeanQueryModelsQueryModificationPackage.value.asJava)
          val fileQueryTransform = new org.avaje.ebean.typequery.agent.offline.OfflineFileTransform(queryTransform, classLoader, classes.getAbsolutePath)
          //Defaults to null, like the Maven plugin
          fileQueryTransform.process(playEbeanQueryProcessPackages.value.orNull)
        }
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
    def updateStampForClassFile(classFile: File, stamp: Stamp): Stamp = stamp match {
      case _: Exists => Stamp.exists(classFile)
      case _: LastModified => Stamp.lastModified(classFile)
      case _: Hash => Stamp.hash(classFile)
    }
    // Since we may have modified some of the products of the incremental compiler, that is, the compiled template
    // classes and compiled Java sources, we need to update their timestamps in the incremental compiler, otherwise
    // the incremental compiler will see that they've changed since it last compiled them, and recompile them.
    val updatedAnalysis = analysis.copy(stamps = allProducts.foldLeft(analysis.stamps) { (stamps, classFile) =>
      val existingStamp = stamps.product(classFile)
      if (existingStamp == Stamp.notPresent) {
        throw new java.io.IOException("Tried to update a stamp for class file that is not recorded as "
          + s"product of incremental compiler: $classFile")
      }
      stamps.markProduct(classFile, updateStampForClassFile(classFile, existingStamp))
    })

    result.copy(analysis = updatedAnalysis)
  }


  private def configuredEbeanModels = Def.task {
    import collection.JavaConverters._
    import java.util.{ Map => JMap, List => JList }

    // Creates a classloader with all the dependencies and all the resources, from there we can use the play ebean
    // code to load the config as it would be loaded in production
    def withClassLoader[T](block: ClassLoader => T): T = {
      val classpath = unmanagedResourceDirectories.value.map(_.toURI.toURL) ++ dependencyClasspath.value.map(_.data.toURI.toURL)
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
      val configLoader = classLoader.loadClass("play.db.ebean.ModelsConfigLoader").
        asSubclass(classOf[java.util.function.Function[ClassLoader, JMap[String, JList[String]]]])
      val config = configLoader.newInstance().apply(classLoader)

      if (config.isEmpty) {
        Seq("models.*")
      } else {
        config.asScala.flatMap(_._2.asScala).toSeq.distinct
      }
    }
  }

  private def readResourceProperty(resource: String, property: String): String = {
    val props = new java.util.Properties
    val stream = getClass.getClassLoader.getResourceAsStream(resource)
    try { props.load(stream) }
    catch { case e: Exception => }
    finally { if (stream ne null) stream.close }
    props.getProperty(property)
  }
}
