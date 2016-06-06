name := "findbugs-html"

organization := "uk.co.josephearl"

version := "2.4.1"

findbugsFailOnError := true

findbugsReportType := Some(FindBugsReportType.Html)

findbugsReportPath := Some(target.value / "findbugs-report.html")
