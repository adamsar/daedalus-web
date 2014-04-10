name := "daedalus-web"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"  exclude("org.scala-stm", "scala-stm_2.10.0"),
  "net.databinder.dispatch" %% "dispatch-core" % "0.11.0",
  "org.scalamock" %% "scalamock-scalatest-support" % "latest.integration",
  "org.scaldi" %% "scaldi-play" % "0.3.1",
  "org.apache.commons" % "commons-io" % "1.3.2"
)

play.Project.playScalaSettings
