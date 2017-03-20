name := """play-scala"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies += jdbc
libraryDependencies += cache
libraryDependencies += ws
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
libraryDependencies += "org.apache.commons" % "commons-math3" % "3.6.1"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.6.3"
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.0.1",
  "org.apache.spark" %% "spark-sql" % "2.0.1",
  "org.apache.spark" %% "spark-streaming" % "2.0.1",
  "org.apache.spark" %% "spark-hive" % "2.0.1",
  "org.apache.spark" %% "spark-mllib" % "2.0.1"
)

fork := true // required for "sbt run" to pick up javaOptions

// IntelliJ integration
javaOptions += "-Dplay.editor=http://localhost:63342/api/file/?file=%s&line=%s"