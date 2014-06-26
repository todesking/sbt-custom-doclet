package com.todesking.example

object Build extends sbt.Build{
  import sbt._
  import com.todesking.sbt_custom_doc.Plugin.autoImport._

  lazy val root = Project(
    "example",
    file("."),
    settings = Seq(
      customDocGenerator := "com.todesking.example.DocGen"
    )
  )
}

class DocGen extends scala.tools.nsc.doc.doclet.Generator {
  override def generateImpl():Unit = {
    println("This is a sample doclet")
  }
}
