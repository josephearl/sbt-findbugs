sbtPlugin := true

name := "sbt-findbugs"

organization := "uk.co.josephearl"

version := "3.0.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "net.sf.saxon" % "Saxon-HE" % "9.4",
  "org.scalatest" % "scalatest_2.10" % "2.2.1" % "test",
  "junit" % "junit" % "4.11" % "test"
)

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

licenses += ("EPL-1.0", url("https://www.eclipse.org/legal/epl-v10.html"))

publishMavenStyle := false

publishArtifact in Test := false
