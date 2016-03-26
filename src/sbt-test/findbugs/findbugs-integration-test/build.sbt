name := "findbugs-test"

organization := "com.lenioapp"

version := "2.2.0"

lazy val root = (project in file(".")).configs(IntegrationTest)

Defaults.itSettings

findbugs in IntegrationTest <<= findbugsTask(IntegrationTest)
findbugsReportPath in IntegrationTest := Some(target(_ / "findbugs-integration-test-report.xml").value)
findbugsAnalyzedPath in IntegrationTest := Seq((classDirectory in IntegrationTest).value)
findbugsAuxiliaryPath in IntegrationTest := (dependencyClasspath in IntegrationTest).value.files
