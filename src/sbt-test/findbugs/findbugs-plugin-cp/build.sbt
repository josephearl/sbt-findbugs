import com.lenioapp.sbt.findbugs._

name := "findbugs-plugin-cp"

organization := "com.lenioapp"

version := "2.0.0-SNAPSHOT"

libraryDependencies += "com.mebigfatguy.fb-contrib" % "fb-contrib" % "6.6.0"

FindBugs.failOnError := true

FindBugs.pluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.mebigfatguy.fb-contrib/fb-contrib/jars/fb-contrib-6.6.0.jar"
