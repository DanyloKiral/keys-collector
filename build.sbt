name := "key-collector-project"

version := "1.0"

scalaVersion := "2.13.4"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime

val AkkaVersion = "2.6.10"
val AkkaHttpVersion = "10.2.2"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream-typed" % AkkaVersion
)

libraryDependencies += "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "2.0.2"
libraryDependencies += "org.postgresql" % "postgresql" % "9.4-1206-jdbc42"