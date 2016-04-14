sbtPlugin := true

name := "sbt-findbugs-plugin"

organization := "com.lenioapp"

version := "2.3.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

licenses += ("EPL-1.0", url("https://www.eclipse.org/legal/epl-v10.html"))

publishMavenStyle := false

publishArtifact in Test := false
