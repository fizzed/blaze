lazy val root = (project in file(".")).
  settings(
    name := "fizzed-blaze",
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.11.4",
    resolvers += Resolver.mavenLocal,
    libraryDependencies += "org.zeroturnaround" % "zt-exec" % "1.7",
    libraryDependencies += "org.apache.ivy" % "ivy" % "2.4.0-rc1",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2",
    libraryDependencies += "co.fizzed" % "fizzed-stork-launcher" % "1.2.0-SNAPSHOT",
    libraryDependencies += "junit" % "junit" % "4.+" % "test"
  )

// java-only project
autoScalaLibrary := false

crossPaths := false

unmanagedSourceDirectories in Compile <<= Seq(javaSource in Compile).join

// stork-generate

val storkify = taskKey[Unit]("Generates stork launchers and assembly")

storkify := {
  val logger = streams.value.log
  Process("../java-stork/cli/target/stork/bin/stork-launcher-generate -o target/stork -i src/main/launchers") ! logger match {
    case 0 => // Success!
    case n => sys.error(s"Could not generate launchers, exit code: $n")
  }
  // copy dependencies
//  val jars = libraryDependencies.libraries
//  FileUtilities.copyFlat(jars.get, target(_ / "stork" / "lib"), log)
}
