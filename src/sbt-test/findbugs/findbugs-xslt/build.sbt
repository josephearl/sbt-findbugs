name := "findbugs-report"

organization := "com.lenioapp"

version := "2.3.0"

findbugsXsltTransformations := Some(Set(FindBugsXSLTTransformation(baseDirectory(_ / "xsl" / "default.xsl").value, target(_ / "findbugs-report.html").value)))

