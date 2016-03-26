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
package de.johoop.findbugs4sbt

import java.io.File

import de.johoop.findbugs4sbt.Effort.Effort
import de.johoop.findbugs4sbt.Priority.Priority
import de.johoop.findbugs4sbt.ReportType._
import sbt._
import sbt.Keys._

import scala.xml.Node

object FindBugs extends AutoPlugin with CommandLine with CommandLineExecutor {
  override def trigger: PluginTrigger = allRequirements

  val findbugs = TaskKey[Unit]("findbugs")
  val findbugsClasspath = TaskKey[Classpath]("findbugs-classpath")
  val findbugsPathSettings = TaskKey[PathSettings]("findbugs-path-settings")
  val findbugsFilterSettings = TaskKey[FilterSettings]("findbugs-filter-settings")
  val findbugsMiscSettings = TaskKey[MiscSettings]("findbugs-misc-settings")
  /** Output path for FindBugs reports. Defaults to <code>Some(crossTarget / "findbugs" / "findbugs.xml")</code>. */
  val findbugsReportPath = SettingKey[Option[File]]("findbugs-report-path")
  /** The path to the classes to be analyzed. Defaults to <code>target / classes</code>. */
  val findbugsAnalyzedPath = TaskKey[Seq[File]]("findbugs-analyzed-path")
  /** The path to the classes not to be analyzed but referenced by analyzed ones. Defaults to <code>dependencyClasspath in Compile</code>. */
  val findbugsAuxiliaryPath = TaskKey[Seq[File]]("findbugs-auxiliary-path")
  /** Type of report to create. Defaults to <code>Some(ReportType.Xml)</code>. */
  val findbugsReportType = SettingKey[Option[ReportType]]("findbugs-report-type")
  /** Priority of bugs shown. Defaults to <code>Priority.Medium</code>. */
  val findbugsPriority = SettingKey[Priority]("findbugs-priority")
  /** Effort put into bug finding. Defaults to <code>Effort.Default</code> */
  val findbugsEffort = SettingKey[Effort]("findbugs-effort")
  /** Optionally, define which packages/classes should be analyzed (<code>None</code> by default) */
  val findbugsOnlyAnalyze = SettingKey[Option[Seq[String]]]("findbugs-only-analyze")
  /** Maximum amount of memory to allow for FindBugs (in MB). */
  val findbugsMaxMemory = SettingKey[Int]("findbugs-max-memory")
  /** Whether FindBugs should analyze nested archives or not. Defaults to <code>true</code>. */
  val findbugsAnalyzeNestedArchives = SettingKey[Boolean]("findbugs-analyze-nested-archives")
  /** Whether the reported bug instances should be sorted by class name or not. Defaults to <code>false</code>.*/
  val findbugsSortReportByClassNames = SettingKey[Boolean]("findbugs-sort-report-by-class-names")
  /** Whether to fail the build if errors are found. Defaults to <code>false</code>.*/
  val findbugsFailOnError = SettingKey[Boolean]("findbugs-fail-on-error")
  /** Optional filter file XML content defining which bug instances to include in the static analysis.
    * <code>None</code> by default. */
  val findbugsIncludeFilters = TaskKey[Option[Node]]("findbugs-include-filter")
  /** Optional filter file XML content defining which bug instances to exclude in the static analysis.
    * <code>None</code> by default. */
  val findbugsExcludeFilters = TaskKey[Option[Node]]("findbugs-exclude-filter")

  private val findbugsConfig = config("findbugs") hide

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

  def findbugsSettings: Seq[Def.Setting[_]] = Seq(
    ivyConfigurations += findbugsConfig
  )

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    findbugs <<= (findbugsClasspath, managedClasspath in Compile,
      findbugsPathSettings, findbugsFilterSettings, findbugsMiscSettings, javaHome, streams) map findbugsTask,

    ivyConfigurations += findbugsConfig,
    libraryDependencies ++= Seq(
      "com.google.code.findbugs" % "findbugs" % "3.0.0" % "findbugs->default",
      "com.google.code.findbugs" % "jsr305" % "3.0.0" % "findbugs->default"
    ),

    findbugsPathSettings <<= (findbugsReportPath, findbugsAnalyzedPath, findbugsAuxiliaryPath) map PathSettings dependsOn (compile in Compile),
    findbugsFilterSettings <<= (findbugsIncludeFilters, findbugsExcludeFilters) map FilterSettings,
    findbugsMiscSettings <<= (findbugsReportType, findbugsPriority, findbugsOnlyAnalyze, findbugsMaxMemory,
      findbugsAnalyzeNestedArchives, findbugsSortReportByClassNames, findbugsEffort, findbugsFailOnError) map MiscSettings,

    findbugsClasspath := Classpaths managedJars (findbugsConfig, classpathTypes value, update value),

    findbugsReportType := Some(ReportType.Xml),
    findbugsPriority := Priority.Medium,
    findbugsEffort := Effort.Default,
    findbugsReportPath := Some(crossTarget.value / "findbugs" / "report.xml"),
    findbugsMaxMemory := 1024,
    findbugsAnalyzeNestedArchives := true,
    findbugsSortReportByClassNames := false,
    findbugsFailOnError := false,
    findbugsAnalyzedPath := Seq(classDirectory in Compile value),
    findbugsAuxiliaryPath := (dependencyClasspath in Compile).value.files,
    findbugsOnlyAnalyze := None,
    findbugsIncludeFilters := None,
    findbugsExcludeFilters := None
  )
}
