name := "neuralFPGA"

version := "1.0"

scalaVersion := "2.11.12"

EclipseKeys.withSource := true

libraryDependencies ++= Seq(
  "com.github.spinalhdl" %% "spinalhdl-core" % "1.3.1",
  "com.github.spinalhdl" %% "spinalhdl-lib" % "1.3.1",
  "org.scalactic" %% "scalactic" % "3.0.5",
  "org.scalatest" %% "scalatest" % "3.0.5" % "test"
)

fork := true
