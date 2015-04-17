import sbt.Keys._
import sbt._
import sbtassembly.Plugin._


// activating assembly plugin
assemblySettings

name := "songengine"

version := "1.0"

scalaVersion := "2.10.0"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2-M1"

libraryDependencies += "uk.co.caprica" % "vlcj" % "2.4.1"

unmanagedBase := baseDirectory.value / "lib"

libraryDependencies += "com.typesafe.akka" % "akka-remote_2.10" % "2.3.9"

libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.10" % "2.3.9"

libraryDependencies += "io.spray" % "spray-can" % "1.3.1"

libraryDependencies += "net.java.dev.jna" % "jna" % "4.1.0"

libraryDependencies += "log4j" % "log4j" % "1.2.17"


