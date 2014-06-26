# sbt-custom-doc: The SBT plugin

This plugin enables scaladoc's `-doc-generator` feature.


## USAGE

```scala
// project/Build.scala
package com.todesking.example

object Build extends sbt.Build{
  import sbt._
  import com.todesking.sbt_custom_doc.Plugin.autoImport._

  lazy val root = Project(
    "example",
    file("."),
    settings = Seq(
      customDocGeneratorClass := Some(classOf[DocGen])
    )
  )
}

class DocGen extends scala.tools.nsc.doc.doclet.Generator {
  override def generateImpl():Unit = {
    println("This is a sample doclet")
  }
}
```

and use `customDoc` task.

