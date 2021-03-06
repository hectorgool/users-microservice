name := """users-microservice"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "com.twitter" % "finagle-http_2.11"   % "6.28.0",
  "com.twitter" % "bijection-util_2.11" % "0.8.1"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

//https://www.playframework.com/documentation/2.4.x/CorsFilter
libraryDependencies += filters

play.PlayImport.PlayKeys.playDefaultPort := 9000