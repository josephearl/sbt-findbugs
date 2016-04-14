/*
 * Copyright (c) 2015-2016 Lenio Ltd, Joseph Earl & contributors.
 * All rights reserved.
 *
 * Copyright (c) 2010-2014 Joachim Hofer & contributors
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package com.lenioapp.sbt.findbugs

import java.io.File

import com.lenioapp.sbt.findbugs.FindBugsReportType.FindBugsReportType
import sbt.Def.Initialize
import sbt.Keys._
import sbt._

import scala.xml.Node

object FindBugsPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = allRequirements

  object autoImport {
    val findbugs = TaskKey[Unit]("findbugs")
    val findbugsClasspath = TaskKey[Classpath]("findbugs-classpath")
    /** Plugin list for FindBugs. Defaults to <code>Seq()</code>. */
    val findbugsPluginList = TaskKey[Seq[String]]("findbugs-plugin-list")
    /** Output path for FindBugs reports. Defaults to <code>Some(target / "findbugs" / "findbugs.xml")</code>. */
    val findbugsReportPath = SettingKey[Option[File]]("findbugs-report-path")
    /** The path to the classes to be analyzed. Defaults to <code>target / classes</code>. */
    val findbugsAnalyzedPath = TaskKey[Seq[File]]("findbugs-analyzed-path")
    /** The path to the classes not to be analyzed but referenced by analyzed ones. Defaults to <code>dependencyClasspath in Compile</code>. */
    val findbugsAuxiliaryPath = TaskKey[Seq[File]]("findbugs-auxiliary-path")
    /** Type of report to create. Defaults to <code>Some(ReportType.Xml)</code>. */
    val findbugsReportType = SettingKey[Option[FindBugsReportType]]("findbugs-report-type")
    /** Priority of bugs shown. Defaults to <code>Priority.Medium</code>. */
    val findbugsPriority = SettingKey[FindBugsPriority]("findbugs-priority")
    /** Effort put into bug finding. Defaults to <code>Effort.Default</code> */
    val findbugsEffort = SettingKey[FindBugsEffort]("findbugs-effort")
    /** Optionally, define which packages/classes should be analyzed (<code>None</code> by default) */
    val findbugsOnlyAnalyze = SettingKey[Option[Seq[String]]]("findbugs-only-analyze")
    /** Maximum amount of memory to allow for FindBugs (in MB). */
    val findbugsMaxMemory = SettingKey[Int]("findbugs-max-memory")
    /** Whether FindBugs should analyze nested archives or not. Defaults to <code>true</code>. */
    val findbugsAnalyzeNestedArchives = SettingKey[Boolean]("findbugs-analyze-nested-archives")
    /** Whether the reported bug instances should be sorted by class name or not. Defaults to <code>false</code>. */
    val findbugsSortReportByClassNames = SettingKey[Boolean]("findbugs-sort-report-by-class-names")
    /** Whether to fail the build if errors are found. Defaults to <code>false</code>. */
    val findbugsFailOnError = SettingKey[Boolean]("findbugs-fail-on-error")
    /** Optional filter file XML content defining which bug instances to include in the static analysis.
      * <code>None</code> by default. */
    val findbugsIncludeFilters = TaskKey[Option[Node]]("findbugs-include-filter")
    /** Optional filter file XML content defining which bug instances to exclude in the static analysis.
      * <code>None</code> by default. */
    val findbugsExcludeFilters = TaskKey[Option[Node]]("findbugs-exclude-filter")
    /** Optional set of XSLT transformations to be applied to the report.
      * <code>None</code> by default. */
    val findbugsXsltTransformations = SettingKey[Option[Set[FindBugsXSLTTransformation]]]("findbugs-xslt-transformations")

    // Type aliases
    val FindBugsReportType = com.lenioapp.sbt.findbugs.FindBugsReportType
    val FindBugsPriority = com.lenioapp.sbt.findbugs.FindBugsPriority
    val FindBugsEffort = com.lenioapp.sbt.findbugs.FindBugsEffort
    val FindBugsXSLTTransformation = com.lenioapp.sbt.findbugs.FindBugsXSLTTransformation

    /**
      * Runs findbugs
      *
      * @param conf The configuration (Compile or Test) in which context to execute the checkstyle command
      */
    def findbugsTask(conf: Configuration): Initialize[Task[Unit]] = Def.task {
      val filterSettings = ((findbugsIncludeFilters in conf, findbugsExcludeFilters in conf) map FilterSettings).value
      val pathSettings = ((findbugsReportPath in conf, findbugsAnalyzedPath in conf, findbugsAuxiliaryPath in conf) map PathSettings dependsOn (compile in conf)).value
      val miscSettings = ((findbugsReportType, findbugsPriority, findbugsOnlyAnalyze, findbugsMaxMemory,
        findbugsAnalyzeNestedArchives, findbugsSortReportByClassNames, findbugsEffort, findbugsFailOnError,
        findbugsPluginList, findbugsXsltTransformations) map MiscSettings).value

      FindBugs.findbugs(findbugsClasspath.value, (managedClasspath in Compile).value,
        pathSettings, filterSettings, miscSettings, javaHome.value, streams.value)
    }
  }

  private val findbugsConfig = config("findbugs").hide

  import autoImport._

  override def projectConfigurations: Seq[Configuration] = Seq(
    findbugsConfig
  )

  private lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
    findbugsClasspath := Classpaths managedJars (findbugsConfig, classpathTypes.value, update.value),
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "findbugs" % "3.0.1" % "findbugs->default",
      "com.google.code.findbugs" % "jsr305" % "3.0.1" % "findbugs->default"
    ),
    findbugsPluginList := Seq(),
    findbugsReportType := Some(com.lenioapp.sbt.findbugs.FindBugsReportType.Xml),
    findbugsPriority := com.lenioapp.sbt.findbugs.FindBugsPriority.Medium,
    findbugsEffort := com.lenioapp.sbt.findbugs.FindBugsEffort.Default,
    findbugsMaxMemory := 1024,
    findbugsAnalyzeNestedArchives := true,
    findbugsSortReportByClassNames := false,
    findbugsFailOnError := false,
    findbugsOnlyAnalyze := None,
    findbugsIncludeFilters := None,
    findbugsExcludeFilters := None,
    findbugsXsltTransformations := None
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    findbugs <<= findbugsTask(Compile),
    findbugs in Test <<= findbugsTask(Test),
    findbugsReportPath := Some(target.value / "findbugs-report.xml"),
    findbugsReportPath in Test := Some(target.value / "findbugs-test-report.xml"),
    findbugsAnalyzedPath := Seq((classDirectory in Compile).value),
    findbugsAnalyzedPath in Test := Seq((classDirectory in Test).value),
    findbugsAuxiliaryPath := (dependencyClasspath in Compile).value.files,
    findbugsAuxiliaryPath in Test := (dependencyClasspath in Test).value.files
  ) ++ commonSettings
}
