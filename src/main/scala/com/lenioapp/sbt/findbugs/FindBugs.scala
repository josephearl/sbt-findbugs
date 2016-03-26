/*
 * This file is part of findbugs4sbt.
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
import sbt.Keys._
import sbt._

import scala.xml.Node

object FindBugs extends AutoPlugin with CommandLine with CommandLineExecutor {
  override def trigger: PluginTrigger = allRequirements

  val findbugs = TaskKey[Unit]("findbugs")
  val findbugsClasspath = TaskKey[Classpath]("findbugs-classpath")
  val findbugsPathSettings = TaskKey[PathSettings]("findbugs-path-settings")
  val findbugsFilterSettings = TaskKey[FilterSettings]("findbugs-filter-settings")
  val findbugsMiscSettings = TaskKey[MiscSettings]("findbugs-misc-settings")

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

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "findbugs" % "3.0.0" % "findbugs->default",
      "com.google.code.findbugs" % "jsr305" % "3.0.0" % "findbugs->default"
    ),

    findbugs <<= (findbugsClasspath, managedClasspath in Compile,
      findbugsPathSettings, findbugsFilterSettings, findbugsMiscSettings, javaHome, streams) map findbugsTask,
    findbugs in Test <<= (findbugsClasspath, managedClasspath in Compile,
      findbugsPathSettings in Test, findbugsFilterSettings, findbugsMiscSettings, javaHome, streams) map findbugsTask,

    findbugsPathSettings <<= (reportPath, analyzedPath, auxiliaryPath) map PathSettings dependsOn (compile in Compile),
    findbugsPathSettings in Test <<= (reportPath in Test, analyzedPath in Test, auxiliaryPath in Test) map PathSettings dependsOn (compile in Test),

    findbugsFilterSettings <<= (includeFilters, excludeFilters) map FilterSettings,
    findbugsMiscSettings <<= (reportType, priority, onlyAnalyze, maxMemory,
      analyzeNestedArchives, sortReportByClassNames, effort, failOnError, plugins) map MiscSettings,

    findbugsClasspath := Classpaths managedJars (findbugsConfig, classpathTypes.value, update.value),

    plugins := Seq(),
    reportType := Some(ReportType.Xml),
    priority := Priority.Medium,
    effort := Effort.Default,
    reportPath := Some(target.value / "findbugs-report.xml"),
    reportPath in Test := Some(target.value / "findbugs-test-report.xml"),
    maxMemory := 1024,
    analyzeNestedArchives := true,
    sortReportByClassNames := false,
    failOnError := false,
    analyzedPath := Seq((classDirectory in Compile).value),
    analyzedPath in Test := Seq((classDirectory in Test).value),
    auxiliaryPath := (dependencyClasspath in Compile).value.files,
    auxiliaryPath in Test := (dependencyClasspath in Test).value.files,
    onlyAnalyze := None,
    includeFilters := None,
    excludeFilters := None
  )
}
