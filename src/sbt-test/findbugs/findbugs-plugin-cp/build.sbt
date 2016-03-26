name := "findbugs-plugin-cp"

organization := "com.lenioapp"

version := "2.1.0"

libraryDependencies += "com.mebigfatguy.fb-contrib" % "fb-contrib" % "6.6.0"

findbugsFailOnError := true

findbugsPluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.mebigfatguy.fb-contrib/fb-contrib/jars/fb-contrib-6.6.0.jar"
