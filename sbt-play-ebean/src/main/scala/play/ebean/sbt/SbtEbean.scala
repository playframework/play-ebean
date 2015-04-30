package play.ebean.sbt

import java.net.URLClassLoader

import com.typesafe.play.sbt.enhancer.PlayEnhancer
import sbt.Keys._
import sbt._
import sbt.inc._

import scala.util.control.NonFatal

object Import {
  object EbeanKeys {
    val models = TaskKey[Seq[String]]("ebeanModels", "The packages that should be searched for ebean models to enhance.")
    val playEbeanVersion = SettingKey[String]("playEbeanVersion", "The version of Play ebean that should be added to the library dependencies.")
  }
}

object SbtEbean extends AutoPlugin {
  import Import.EbeanKeys._

  val autoImport = Import

  // Must require PlayEnhancer to make sure it runs before we do
  override def requires = PlayEnhancer

  override def trigger = noTrigger

  override def projectSettings = inConfig(Compile)(scopedSettings) ++ unscopedSettings

  def scopedSettings = Seq(
    models <<= configuredEbeanModels,
    manipulateBytecode <<= ebeanEnhance
  )

  def unscopedSettings = Seq(
    playEbeanVersion := readResourceProperty("play-ebean.version.properties", "play-ebean.version"),
    libraryDependencies += "com.typesafe.play" %% "play-ebean" % playEbeanVersion.value
  )

  def ebeanEnhance: Def.Initialize[Task[Compiler.CompileResult]] = Def.task {

    val deps = dependencyClasspath.value
    val classes = classDirectory.value
    val result = manipulateBytecode.value
    val analysis = result.analysis

    val originalContextClassLoader = Thread.currentThread.getContextClassLoader

    try {

      val classpath = deps.map(_.data.toURI.toURL).toArray :+ classes.toURI.toURL

      Thread.currentThread.setContextClassLoader(new java.net.URLClassLoader(classpath, ClassLoader.getSystemClassLoader))

      import com.avaje.ebean.enhance.agent._
      import com.avaje.ebean.enhance.ant._

      val classloader = ClassLoader.getSystemClassLoader

      val transformer = new Transformer(classpath, "debug=-1")

      val fileTransform = new OfflineFileTransform(transformer, classloader, classes.getAbsolutePath, classes.getAbsolutePath)

      try {
        fileTransform.process(models.value.mkString(","))
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
