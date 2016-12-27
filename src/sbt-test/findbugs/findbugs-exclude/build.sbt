import scala.xml.XML

name := "findbugs-exclude"

organization := "uk.co.josephearl"

version := "2.4.4"

findbugsFailOnError := true

findbugsExcludeFilters := Some(XML.loadFile(baseDirectory.value / "findbugs-exclude-filters.xml"))
