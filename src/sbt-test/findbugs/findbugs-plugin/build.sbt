name := "findbugs-plugin"

organization := "uk.co.josephearl"

version := "2.2.0"

findbugsFailOnError := true

findbugsPluginList += file("lib/fb-contrib-6.6.0.jar").absolutePath
