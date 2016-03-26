import de.johoop.findbugs4sbt.FindBugs

name := "findbugs-auto"

organization := "de.johoop"

version := "1.4.2-SNAPSHOT"

FindBugs.findbugsReportPath := Some(target.value / "findbugs" / "my-findbugs-report.xml")
