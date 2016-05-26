name := "findbugs-html"

organization := "uk.co.josephearl"

version := "2.4.1"

findbugsXsltTransformations := Some(Set(FindBugsXSLTTransformation(baseDirectory(_ / "xsl" / "default.xsl").value, target(_ / "findbugs-report.html").value)))

findbugsReportType := Some(FindBugsReportType.Html)

findbugsReportPath := Some(target.value / "findbugs-report.html")
