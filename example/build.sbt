scalaVersion := "2.10.4"

scalacOptions in (Compile, doc) ++= Opts.doc.generator("com.todesking.reusable_doclet.ReusableDoclet")

libraryDependencies += "com.todesking" %% "reusable_doclet" % "0.0.1"

customDocRenderer := "hoge"

