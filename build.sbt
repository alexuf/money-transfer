
scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.module" %% "jackson-module-scala_2.12" % "2.8.7",
  "com.twitter" %% "finagle-http" % "6.43.0",

  "org.mockito" % "mockito-core" % "2.7.17" % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)