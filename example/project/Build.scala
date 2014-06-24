object Build extends sbt.Build {
  import sbt._

  val customDoc = taskKey[Unit]("Custom doc")

  val customDocRenderer = settingKey[String]("renderer class name")

  val customDocTask = customDoc := {
    // from sbt.compiler.AnalyzingCompiler implementation
    val compiler:sbt.compiler.AnalyzingCompiler = Keys.compilers.value.scalac
    val dependencyClasspath = (Keys.fullClasspath in (Compile, Keys.doc)).value.map(_.data)
    val options = (Keys.scalacOptions).value ++ Opts.doc.externalAPI(Keys.apiMappings.value) ++ Seq("-doc-generator", "com.todesking.reusable_doclet.ReusableDoclet")

    val arguments = (new sbt.compiler.CompilerArguments(compiler.scalaInstance, compiler.cp))(
      (Keys.sources in (Compile, Keys.doc)).value, dependencyClasspath, Some(Keys.target.value), options)

    val klass = getInterfaceClass("xsbt.ScaladocInterface", compiler, Keys.streams.value.log, dependencyClasspath)
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

  def getInterfaceClass(name:String, compiler:sbt.compiler.AnalyzingCompiler, log:Logger, cp:Seq[File]) = {
    val url = compiler.provider(compiler.scalaInstance, log).toURI.toURL
    val urls = Seq(url) ++ cp.map(_.toURI.toURL)
    val loader = new java.net.URLClassLoader(urls.toArray, createDualLoader(compiler.scalaInstance.loader, getClass().getClassLoader())) {
      override def loadClass(name:String):Class[_] = {
        val c:Class[_] = findLoadedClass(name)
        if(c != null) return c

        try findClass(name)
        catch { case e:ClassNotFoundException => super.loadClass(name) }
      }
    }

    loader.loadClass(name)
  }

  protected def createDualLoader(scalaLoader: ClassLoader, sbtLoader: ClassLoader): ClassLoader =
  {
    val xsbtiFilter = (name: String) => name.startsWith("xsbti.")
    val notXsbtiFilter = (name: String) => !xsbtiFilter(name)
    new classpath.DualLoader(scalaLoader, notXsbtiFilter, x => true, sbtLoader, xsbtiFilter, x => false)
  }


  lazy val root = Project("example", file("."), settings = Defaults.defaultSettings ++ Seq(customDocTask))

}
