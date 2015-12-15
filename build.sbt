sbtPlugin := true

name := "sbt-findbugs-plugin"

organization := "de.johoop"

version := "1.4.2-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-language:_")

publishTo := {
  val nexus = "https://nexus.lenioapp.com/repository/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "lenio-snapshot")
  else
    Some("releases"  at nexus + "lenio-release")
}
