lazy val root = (project in file(".")).enablePlugins(PlayScala).settings(
  name := """play-scala""",
  organization := "com.datacentral",
  scalaVersion := "2.11.8",
  version := "0.1.0-SNAPSHOT",
  libraryDependencies ++= Seq(jdbc, cache, ws,
    "com.github.dwickern" %% "scala-nameof" % "1.0.3" % "provided",
    "com.lihaoyi" %% "scalatags" % "0.6.3",
    "org.apache.commons" % "commons-math3" % "3.6.1",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
    "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-streaming" % sparkVersion,
    "org.apache.spark" %% "spark-hive" % sparkVersion,
    "org.apache.spark" %% "spark-mllib" % sparkVersion,
    "org.json" % "json" % "20170516",
    "org.jsoup" % "jsoup" % "1.10.3",
    "com.neovisionaries" % "nv-i18n" % "1.22",
    "com.rometools" % "rome" % "1.7.3",
    "net.sourceforge.htmlunit" % "htmlunit" % "2.13",
    "com.typesafe.akka" % "akka-actor_2.11" % "2.5.3",
    "com.typesafe.slick" %% "slick" % "3.2.0",
    "com.typesafe.slick" %% "slick-hikaricp" % "3.2.0",
    "org.scalaz" % "scalaz-core_2.11" % "7.2.14",
    "org.scalaz" % "scalaz-concurrent_2.11" % "7.2.14"),
  printKeys := {
    import reflect.runtime._
    import universe._
    lazy val instanceMirror: InstanceMirror = currentMirror reflect Keys
    lazy val symbols: Seq[Symbol] = instanceMirror
      .symbol.asClass.typeSignature.members
      .filter(s => s.isTerm && s.asTerm.isAccessor)
      .toSeq.sortBy(_.fullName)
    lazy val names: Seq[String] = symbols.map(_.fullName)
    lazy val values: Seq[String] = symbols
      .map(instanceMirror reflectMethod _.asMethod)
      .map(_.apply().toString)
    println(names.zip(values)
      .map(field => s"${field._1} := ${field._2}")
      .mkString("\n"))
    val ur = update.value
    // update task happens-before printKeys because .value is not a normal Scala method call. build.sbt DSL uses a macro to lift these outside of the task body. Both update and clean tasks are completed by the time task engine evaluates the opening { of scalacOptions regardless of which line it appears in the body.
    val x = clean.value // Another important thing to note is that there’s no guarantee about the ordering of update and clean tasks. They might run update then clean, clean then update, or both in parallel.
  },
  // Scala Options
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-encoding", "utf8", "-Xfatal-warnings"),
  scalacOptions := {
    val old = scalacOptions.value
    scalaBinaryVersion.value match {
      case "2.12" => old
      case _ => old.filterNot(Set("-Xfatal-warnings", "-deprecation").apply)
    }
  }
)
// Example of custom task definition
lazy val printKeys = taskKey[Unit]("Project Info")
lazy val sparkVersion = "2.2.0"
// required for "sbt run" to pick up javaOptions
fork := true
// IntelliJ integration
javaOptions += "-Dplay.editor=http://localhost:63342/api/file/?file=%s&line=%s"