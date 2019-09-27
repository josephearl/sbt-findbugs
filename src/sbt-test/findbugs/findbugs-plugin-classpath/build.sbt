name := "findbugs-plugin-classpath"

organization := "uk.co.josephearl"

version := "2.2.0"

libraryDependencies += "com.mebigfatguy.fb-contrib" % "fb-contrib" % "7.4.6"

findbugsFailOnError := true

findbugsPluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.mebigfatguy.fb-contrib/fb-contrib/jars/fb-contrib-7.4.6.jar"
