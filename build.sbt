name := "scaladown"
version := "0.0.0"
scalaVersion := "2.13.3"
useCoursier := false
libraryDependencies ++= Seq(
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "dev.zio" %% "zio" % "1.0.9",
  "org.scala-lang.modules" %% "scala-xml" % "2.0.0",
  "org.planet42" %% "laika-io" % "0.17.1"
)
