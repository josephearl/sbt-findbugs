name := "findbugs-plugin"

organization := "uk.co.josephearl"

version := "2.2.0"

findbugsFailOnError := true

findbugsPluginList += file("lib/fb-contrib-7.4.6.jar").absolutePath
