import scala.xml.XML

name := "findbugs-exclude"

organization := "uk.co.josephearl"

version := "3.0.0-SNAPSHOT"

findbugsFailOnError := true

findbugsExcludeFilters := Some(XML.loadFile(baseDirectory.value / "findbugs-exclude-filters.xml"))
