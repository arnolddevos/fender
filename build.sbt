name := "fender"

organization := "com.bgsig"

versionWithGit

git.baseVersion := "0.9"

libraryDependencies += "org.eclipse.jetty" % "jetty-server" % "9.3.0.M2"

libraryDependencies += "org.eclipse.jetty" % "jetty-client" % "9.3.0.M2"

libraryDependencies += "org.eclipse.jetty" % "jetty-jmx" % "9.3.0.M2"

libraryDependencies += "org.eclipse.jetty" % "jetty-continuation" % "9.3.0.M2"

lazy val bumpDeps = taskKey[Unit]("capture the versions of dependencies")

bumpDeps := {
  for(dep <- List("anodyne")) 
    IO.copyFile(file(s"../$dep/dependency"), file(s"$dep.sbt"))
}

sourcesInBase := false

