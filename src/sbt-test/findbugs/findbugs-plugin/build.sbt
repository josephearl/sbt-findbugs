name := "findbugs-plugin"

organization := "com.lenioapp"

version := "2.1.0"

findbugsFailOnError := true

findbugsPluginList += file("lib/fb-contrib-6.6.0.jar").absolutePath
