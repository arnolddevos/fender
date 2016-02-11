enablePlugins(GitVersioning)

publishLocal := { 
  def depends = 
    s""""${organization.value}" %% "${name.value}" % "${version.value}""""
  IO.write(file(s"${target.value}/${name.value}.sbt"), s"libraryDependencies += $depends\n")
  publishLocal.value 
}

git.formattedShaVersion := {
  if(git.gitUncommittedChanges.value) None else git.formattedShaVersion.value
}
