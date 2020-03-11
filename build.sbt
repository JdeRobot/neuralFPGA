val spinalVersion = "1.4.0"
lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.github.jderobot",
      scalaVersion := "2.11.12",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "neuralFPGA",
    scalaSource in Compile := baseDirectory.value / "hardware" / "main" / "scala",
    scalaSource in Test    := baseDirectory.value / "hardware" / "test" / "scala",
    libraryDependencies ++= Seq(
      "com.github.spinalhdl" % "spinalhdl-core_2.11" % spinalVersion,
      "com.github.spinalhdl" % "spinalhdl-lib_2.11" % spinalVersion,
      compilerPlugin("com.github.spinalhdl" % "spinalhdl-idsl-plugin_2.11" % spinalVersion),
      "org.scalactic" %% "scalactic" % "3.0.5",
      "org.scalatest" %% "scalatest" % "3.0.5"
    )
  ).dependsOn(vexRiscv)

lazy val vexRiscv = RootProject(uri("git://github.com/SpinalHDL/VexRiscv.git#master"))

fork := true