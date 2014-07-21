package com.todesking.sbt_custom_doc

object Plugin extends sbt.AutoPlugin {
  import sbt._

  object autoImport {
    val customDoc = taskKey[Unit]("Custom doc task")
    val customDocGeneratorClass = settingKey[Class[_]]("Doc generator class")
  }

  import autoImport._

  override def trigger = allRequirements

  override val projectSettings = Seq(
    customDoc := {
      // from sbt.compiler.AnalyzingCompiler implementation
      val compiler:sbt.compiler.AnalyzingCompiler = Keys.compilers.value.scalac
      val log = Keys.streams.value.log
      val dependencyClasspath = (Keys.fullClasspath in (Compile, Keys.doc)).value.map(_.data)
      val generatorName = customDocGeneratorClass.value.getName
      val options = (Keys.scalacOptions in (Compile, Keys.doc)).value ++ Opts.doc.externalAPI(Keys.apiMappings.value) ++ Seq("-doc-generator", generatorName)

      log.info(s"customDoc generator class: $generatorName")

      val arguments = (new sbt.compiler.CompilerArguments(compiler.scalaInstance, compiler.cp))(
       (Keys.sources in (Compile, Keys.doc)).value, dependencyClasspath, Some(Keys.target.value), options)

      val loaderClasspath = Seq(resourceURLOf(customDocGeneratorClass.value))

      log.info(s"customDoc additional classpath: $loaderClasspath")

      val loader = createClassLoader(compiler, log, loaderClasspath)

      try { loader.loadClass(generatorName) }
      catch { case e:ClassNotFoundException => throw new RuntimeException(s"customDoc generator class load failed: $generatorName (classpath: ${loader.getURLs.mkString(", ")})") }

      val klass = loader.loadClass("xsbt.ScaladocInterface")
      val instance = klass.newInstance().asInstanceOf[AnyRef]
      val method = klass.getMethod("run", classOf[Array[String]], classOf[xsbti.Logger], classOf[xsbti.Reporter])

      try { method.invoke(instance, arguments.toArray[String], Keys.streams.value.log, new LoggerReporter(99, Keys.streams.value.log)) }
      catch { case e: java.lang.reflect.InvocationTargetException =>
        e.getCause match {
          case c: xsbti.CompileFailed => throw new sbt.compiler.CompileFailed(c.arguments, c.toString, c.problems)
          case t => throw t
        }
      }
    }
  )

  private[this] def resourceURLOf(klass:Class[_]):java.net.URL = {
    // OH....
    val resourceName = klass.getName.split("\\.").mkString("/") + ".class"
    val location = klass.getClassLoader.getResource(resourceName).toString
    val root = location.substring(0, location.length - resourceName.length)
    if(root.endsWith("!")) // jar
      new java.net.URL(root.substring(0, root.length - 1))
    else
      new java.net.URL(root)
  }

  private[this] def createClassLoader(compiler:sbt.compiler.AnalyzingCompiler, log:Logger, cp:Seq[URL]):java.net.URLClassLoader = {
    val url = compiler.provider(compiler.scalaInstance, log).toURI.toURL
    val urls = cp ++ compiler.scalaInstance.allJars.map(_.toURI.toURL) ++ Seq(url)

    new java.net.URLClassLoader(urls.toArray, createDualLoader(compiler.scalaInstance.loader, getClass().getClassLoader())) {
      override def loadClass(name:String):Class[_] = {
        val c:Class[_] = findLoadedClass(name)
        if(c != null) return c

        try findClass(name)
        catch { case e:ClassNotFoundException => getParent.loadClass(name) }
      }
    }
  }

  private[this] def createDualLoader(scalaLoader: ClassLoader, sbtLoader: ClassLoader): ClassLoader =
  {
    val xsbtiFilter = (name: String) => name.startsWith("xsbti.")
    val notXsbtiFilter = (name: String) => !xsbtiFilter(name)
    new classpath.DualLoader(scalaLoader, notXsbtiFilter, x => true, sbtLoader, xsbtiFilter, x => false)
  }

}
