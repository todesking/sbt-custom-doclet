# sbt-custom-doc: The SBT plugin

This plugin enables scaladoc's `-doc-generator` feature.


## USAGE

1. Setting sbt(described below)
2. use `customDoc` task.

## Enable plugin

```scala
// project/build.sbt
addSbtPlugin("com.todesking" %% "sbt_custom_doc" % "0.0.1")
```

## Use inline doc-generator class

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

## Use outer doc-generator class

```scala
// build.sbt
customDocGeneratorName := "name.of.doc.generator"

libraryDependencies in (Compile, doc) += "name.of.doc" %% "generator" % "1.0.0"
```

I not tested this yet :(
