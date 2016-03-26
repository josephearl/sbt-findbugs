/*
 * Copyright (c) 2015-2016 Lenio Ltd, Joseph Earl & contributors.
 * All rights reserved.
 *
 * Copyright (c) 2010-2014 Joachim Hofer & contributors
 * All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.lenioapp.sbt.findbugs

import java.io.File

import com.lenioapp.sbt.findbugs.ReportType.ReportType
import sbt.Def.Initialize
import sbt.Keys._
import sbt._

import scala.xml.Node

object FindBugs extends AutoPlugin with CommandLine with CommandLineExecutor {
  override def trigger: PluginTrigger = allRequirements

  val findbugs = TaskKey[Unit]("findbugs")
  val findbugsClasspath = TaskKey[Classpath]("findbugs-classpath")
  /** Plugin list for FindBugs. Defaults to <code>Seq()</code>. */
  val plugins = TaskKey[Seq[File]]("findbugs-plugins")
  /** Output path for FindBugs reports. Defaults to <code>Some(crossTarget / "findbugs" / "findbugs.xml")</code>. */
  val reportPath = SettingKey[Option[File]]("findbugs-report-path")
  /** The path to the classes to be analyzed. Defaults to <code>target / classes</code>. */
  val analyzedPath = TaskKey[Seq[File]]("findbugs-analyzed-path")
  /** The path to the classes not to be analyzed but referenced by analyzed ones. Defaults to <code>dependencyClasspath in Compile</code>. */
  val auxiliaryPath = TaskKey[Seq[File]]("findbugs-auxiliary-path")
  /** Type of report to create. Defaults to <code>Some(ReportType.Xml)</code>. */
  val reportType = SettingKey[Option[ReportType]]("findbugs-report-type")
  /** Priority of bugs shown. Defaults to <code>Priority.Medium</code>. */
  val priority = SettingKey[Priority]("findbugs-priority")
  /** Effort put into bug finding. Defaults to <code>Effort.Default</code> */
  val effort = SettingKey[Effort]("findbugs-effort")
  /** Optionally, define which packages/classes should be analyzed (<code>None</code> by default) */
  val onlyAnalyze = SettingKey[Option[Seq[String]]]("findbugs-only-analyze")
  /** Maximum amount of memory to allow for FindBugs (in MB). */
  val maxMemory = SettingKey[Int]("findbugs-max-memory")
  /** Whether FindBugs should analyze nested archives or not. Defaults to <code>true</code>. */
  val analyzeNestedArchives = SettingKey[Boolean]("findbugs-analyze-nested-archives")
  /** Whether the reported bug instances should be sorted by class name or not. Defaults to <code>false</code>.*/
  val sortReportByClassNames = SettingKey[Boolean]("findbugs-sort-report-by-class-names")
  /** Whether to fail the build if errors are found. Defaults to <code>false</code>.*/
  val failOnError = SettingKey[Boolean]("findbugs-fail-on-error")
  /** Optional filter file XML content defining which bug instances to include in the static analysis.
    * <code>None</code> by default. */
  val includeFilters = TaskKey[Option[Node]]("findbugs-include-filter")
  /** Optional filter file XML content defining which bug instances to exclude in the static analysis.
    * <code>None</code> by default. */
  val excludeFilters = TaskKey[Option[Node]]("findbugs-exclude-filter")

  def findbugsTask(conf: Configuration): Initialize[Task[Unit]] = Def.task {
    val filterSettings = ((includeFilters in conf, excludeFilters in conf) map FilterSettings).value
    val pathSettings = ((reportPath in conf, analyzedPath in conf, auxiliaryPath in conf) map PathSettings dependsOn (compile in conf)).value
    val miscSettings = ((reportType, priority, onlyAnalyze, maxMemory,
      analyzeNestedArchives, sortReportByClassNames, effort, failOnError, plugins) map MiscSettings).value

    findbugsTask(findbugsClasspath.value, (managedClasspath in Compile).value,
      pathSettings, filterSettings, miscSettings, javaHome.value, streams.value)
  }

  def findbugsTask(findbugsClasspath: Classpath, compileClasspath: Classpath,
      paths: PathSettings, filters: FilterSettings, misc: MiscSettings, javaHome: Option[File],
      streams: TaskStreams): Unit = {

    val log = streams.log

    IO.withTemporaryDirectory { filterPath =>
      val cmd = commandLine(findbugsClasspath, compileClasspath, paths, filters, filterPath, misc, streams)
      log.debug("FindBugs command line to execute: \"%s\"" format (cmd mkString " "))
      executeCommandLine(cmd, javaHome, log)
    }

    paths.reportPath foreach(f => {
      val warnings = {
        val resultFile = paths.reportPath.get
        val results = scala.xml.XML.loadFile(resultFile)
        results \\ "BugCollection" \\ "BugInstance"
      }

      if (warnings.nonEmpty) {
        val message = s"FindBugs found ${warnings.size} issues"
        if (misc.failOnError) {
          sys.error(message)
        } else {
          log.info(message)
        }
      } else {
        log.info("No issues from findbugs")
      }
    })
  }

  private val findbugsConfig = config("findbugs").hide

  override def projectConfigurations: Seq[Configuration] = Seq(
    findbugsConfig
  )

  private lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
    findbugsClasspath := Classpaths managedJars (findbugsConfig, classpathTypes.value, update.value),
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "findbugs" % "3.0.0" % "findbugs->default",
      "com.google.code.findbugs" % "jsr305" % "3.0.0" % "findbugs->default"
    ),
    plugins := Seq(),
    reportType := Some(ReportType.Xml),
    priority := Priority.Medium,
    effort := Effort.Default,
    maxMemory := 1024,
    analyzeNestedArchives := true,
    sortReportByClassNames := false,
    failOnError := false,
    onlyAnalyze := None,
    includeFilters := None,
    excludeFilters := None
  )

  override lazy val projectSettings: Seq[Def.Setting[_]] = Seq(
    findbugs <<= findbugsTask(Compile),
    findbugs in Test <<= findbugsTask(Test),
    reportPath := Some(target.value / "findbugs-report.xml"),
    reportPath in Test := Some(target.value / "findbugs-test-report.xml"),
    analyzedPath := Seq((classDirectory in Compile).value),
    analyzedPath in Test := Seq((classDirectory in Test).value),
    auxiliaryPath := (dependencyClasspath in Compile).value.files,
    auxiliaryPath in Test := (dependencyClasspath in Test).value.files
  ) ++ commonSettings
}
