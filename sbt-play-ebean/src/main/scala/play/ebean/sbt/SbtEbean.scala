package play.ebean.sbt

import play.PlayJava
import sbt.Keys._
import sbt._
import sbt.compiler.AggressiveCompile
import sbt.inc._

import scala.util.control.NonFatal

object Import {
  object EbeanKeys {
    val models = TaskKey[Seq[String]]("ebean-models", "The packages that should be searched for ebean models to enhance.")
    val playEbeanVersion = SettingKey[String]("play-ebean-version", "The version of Play ebean that should be added to the library dependencies.")
  }
}

object SbtEbean extends AutoPlugin {
  import Import.EbeanKeys._

  val autoImport = Import

  // We require PlayJava first in order to ensure that the play enhancer runs before play ebean enhancer
  override def requires = PlayJava

  override def trigger = allRequirements

  override def projectSettings = inConfig(Compile)(scopedSettings) ++ unscopedSettings


  def scopedSettings = Seq(
    models <<= configuredEbeanModels,
    compile <<= ebeanEnhance
  )

  def unscopedSettings = Seq(
    playEbeanVersion := readResourceProperty("play-ebean.version.properties", "play-ebean.version"),
    libraryDependencies += "com.typesafe.play" %% "play-ebean" % playEbeanVersion.value
  )

  def ebeanEnhance = Def.task {

    val deps = dependencyClasspath.value
    val classes = classDirectory.value
    val analysis = compile.value

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

    val javaClasses = (javaSource.value ** "*.java").get flatMap { sourceFile =>
      analysis.relations.products(sourceFile)
    }

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
    val updatedAnalysis = analysis.copy(stamps = javaClasses.foldLeft(analysis.stamps) { (stamps, classFile) =>
      val existingStamp = stamps.product(classFile)
      if (existingStamp == Stamp.notPresent) {
        throw new java.io.IOException("Tried to update a stamp for class file that is not recorded as "
          + s"product of incremental compiler: $classFile")
      }
      stamps.markProduct(classFile, updateStampForClassFile(classFile, existingStamp))
    })

    // Need to persist the updated analysis.
    val agg = new AggressiveCompile((compileInputs in compile).value.incSetup.cacheFile)
    // Load the old one. We do this so that we can get a copy of CompileSetup, which is the cache compiler
    // configuration used to determine when everything should be invalidated. We could calculate it ourselves, but
    // that would by a heck of a lot of fragile code due to the vast number of things we would have to depend on.
    // Reading it out of the existing file is good enough.
    val existing: Option[(Analysis, CompileSetup)] = agg.store.get()
    // Since we've just done a compile before this task, this should never return None, so don't worry about what to
    // do when it returns None.
    existing.foreach {
      case (_, compileSetup) => agg.store.set(updatedAnalysis, compileSetup)
    }

    updatedAnalysis
  }


  private def configuredEbeanModels = Def.task {
    import com.typesafe.config._
    import collection.JavaConverters._

    val configFile = sys.props.get("config.file").map(new File(_)).orElse {
      val configResource = sys.props.get("config.resource").getOrElse("application.conf")
      unmanagedResources.value.filter(r => r.exists && !r.isDirectory)
        .pair(relativeTo(unmanagedResourceDirectories.value))
        .collectFirst {
          case (file, name) if name == configResource => file
        }
    }

    configFile.fold(Seq("models.*")) { file =>
      val config = ConfigFactory.parseFileAnySyntax(file)
      try {
        config.getConfig("ebean").entrySet.asScala.map(_.getValue.unwrapped.toString).toSeq.distinct
      } catch {
        case e: ConfigException.Missing => Seq("models.*")
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
