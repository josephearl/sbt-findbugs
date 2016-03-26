import com.lenioapp.sbt.findbugs._

name := "findbugs-fail"

organization := "com.lenioapp"

version := "2.0.0"

FindBugs.failOnError := true
