name := "songengine"

version := "1.0"

scalaVersion := "2.10.0"

libraryDependencies += "net.databinder.dispatch" %% "dispatch-core" % "0.11.2"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.2.2"

libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.2.2"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2-M1"

libraryDependencies ++= Seq(
  "net.debasishg" %% "redisclient" % "2.13"
)

libraryDependencies += "uk.co.caprica" % "vlcj" % "2.4.1"

unmanagedBase := baseDirectory.value / "lib"