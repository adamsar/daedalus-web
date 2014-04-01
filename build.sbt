name := "daedalus-web"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2"  exclude("org.scala-stm", "scala-stm_2.10.0")
)

play.Project.playScalaSettings
