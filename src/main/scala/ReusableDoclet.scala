package com.todesking.reusable_doclet

import scala.tools.nsc.doc.doclet.{Generator, Universer, Indexer}

object Plugin extends sbt.AutoPlugin {
}

class ReusableDoclet extends Generator with Universer with Indexer {
  override def generate():Unit = {
    throw new RuntimeException("aaaa")
  }
  override def generateImpl():Unit = {
    println("=================================== REUSABLE =====================================")
    throw new RuntimeException("gya-")
  }
}
