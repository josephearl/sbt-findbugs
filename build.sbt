sbtPlugin := true

name := "sbt-findbugs-plugin"

organization := "com.lenioapp"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

publishMavenStyle := false

publishArtifact in Test := false
