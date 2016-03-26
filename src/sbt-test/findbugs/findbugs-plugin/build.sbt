import com.lenioapp.sbt.findbugs._

name := "findbugs-plugin"

organization := "com.lenioapp"

version := "2.0.0"

FindBugs.failOnError := true

FindBugs.pluginList += file("lib/fb-contrib-6.6.0.jar").absolutePath
