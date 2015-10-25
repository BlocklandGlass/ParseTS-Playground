name := """parsets-playground"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  ws,
  filters,
  specs2 % Test,
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.play" %% "play-slick" % "1.1.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.0",
  "org.postgresql" % "postgresql" % "9.4-1204-jdbc42",
  "com.jsuereth" %% "scala-arm" % "1.4",
  "org.webjars" %% "webjars-play" % "2.4.0-2",
  "org.webjars" % "bootstrap" % "4.0.0-alpha",
  "org.webjars" % "requirejs" % "2.1.20",
  "org.webjars" % "codemirror" % "5.7"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
