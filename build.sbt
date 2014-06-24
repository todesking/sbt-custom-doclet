name := "reusable_doclet"

organization := "com.todesking"

version := "0.0.1"

scalaVersion := "2.10.4"

sbtPlugin := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-compiler" % "2.10.4" % "compile;runtime",
  "org.scala-lang" % "scala-reflect" % "2.10.4" % "compile;runtime",
  "org.scala-lang" % "scala-library" % "2.10.4" % "compile;runtime"
)
