import com.lenioapp.sbt.findbugs._

name := "findbugs-report"

organization := "com.lenioapp"

version := "2.0.0"

FindBugs.reportPath := Some(target.value / "findbugs" / "my-findbugs-report.xml")
