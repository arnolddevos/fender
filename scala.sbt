scalaVersion := "2.11.6"

scalacOptions += "-feature"

scalacOptions += "-unchecked"

scalacOptions += "-deprecation"

crossScalaVersions := Seq("2.10.4", "2.11.6")

// add scala-xml dependency when needed (for Scala 2.11 and newer)
// this mechanism supports cross-version publishing
libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value :+ 
        "org.scala-lang.modules" %% "scala-xml" % "1.0.2"
    case _ =>
      libraryDependencies.value
  }
}
