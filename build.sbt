name := "sns-event-stream"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  "org.clapper" %% "grizzled-slf4j" % "1.0.2",
  "com.amazonaws" % "aws-java-sdk" % "1.8.9.1",
  "com.gu" %% "aws-sdk-scala-wrappers" % "0.1",
  cache,
  ws
)
