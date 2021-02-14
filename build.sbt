val scala3V = "3.0.0-M3"
val zioV = "1.0.4-2"
val circeV = "0.14.0-M3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "dgraph0",
    version := "0.1.0",
    scalaVersion := scala3V,
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % zioV,
      "dev.zio" %% "zio-streams" % zioV,
      "io.circe" %% "circe-core" % circeV,
      "io.circe" %% "circe-parser" % circeV,
      "io.circe" %% "circe-generic" % circeV,
      "io.dgraph" % "dgraph4j" % "20.11.0"
    )
  )
