package com.lenioapp.sbt.findbugs

import java.io.File

import sbt._
import sbt.Keys._

object FindBugs extends Object with CommandLine with CommandLineExecutor {
  def findbugs(findbugsClasspath: Classpath, compileClasspath: Classpath,
      paths: PathSettings, filters: FilterSettings, misc: MiscSettings, javaHome: Option[File],
      streams: TaskStreams): Unit = {
    val log = streams.log

    IO.withTemporaryDirectory { filterPath =>
      val cmd = commandLine(findbugsClasspath, compileClasspath, paths, filters, filterPath, misc, streams)
      log.debug("FindBugs command line to execute: \"%s\"" format (cmd mkString " "))
      executeCommandLine(cmd, javaHome, log)
    }

    if (misc.failOnError) {
      val issuesFound = paths.reportPath
        .map(f => processIssues(log, f.getAbsolutePath, misc.failOnError))
        .sum

      if (issuesFound > 0) {
        log.error(issuesFound + " issue(s) found in FindBugs report: " + paths.reportPath.get + "")
        sys.exit(1)
      }
    }
  }

  private def processIssues(log: Logger, outputLocation: String, failOnError: Boolean): Int = {
    val report = scala.xml.XML.loadFile(file(outputLocation))
    val warnings = report \\ "BugCollection" \\ "BugInstance"

    warnings.map(bug => {
      val bugType = bug.attribute("type").get.head.text
      val source = bug \\ "SourceLine" head
      val filename = source.attribute("sourcepath").get.head.text
      val lineNumber = source.attribute("start").get.head.text

      val errorMessage = {
        val bugPriority = bug.attribute("priority").get.head.text
        val bugCategory = bug.attribute("category").get.head.text

        "priority " + bugPriority + " category " + bugCategory +
          (bug \\ "Class" headOption).map(cls => {
            val source = cls \\ "SourceLine" head
            val filename = source.attribute("sourcepath").get.head.text
            val lineNumber = {
              if (source.attribute("start").isDefined) {
                ":" + source.attribute("start").get.head.text +
                  "-" + source.attribute("end").get.head.text
              } else {
                ""
              }
            }
            "\nClass " + cls.attribute("classname").get.head.text + " found in " + filename +
              lineNumber
          }).getOrElse("") +
          (bug \\ "Field" headOption).map(field => {
            val signature = field.attribute("signature").get.head.text
            val isStatic = field.attribute("isStatic").get.head.text
            val source = field \\ "SourceLine" head
            val filename = source.attribute("sourcepath").get.head.text
            val lineNumber = {
              if (source.attribute("start").isDefined) {
                ":" + source.attribute("start").get.head.text +
                  "-" + source.attribute("end").get.head.text
              } else {
                ""
              }
            }
            "\nField " + field.attribute("name").get.head.text + " in class " +
              field.attribute("classname").get.head.text + " found in " + filename +
              lineNumber + ": signature " + signature + " static " + isStatic
          }).getOrElse("") +
          (bug \\ "Method" headOption).map(method => {
            val signature = method.attribute("signature").get.head.text
            val isStatic = method.attribute("isStatic").get.head.text
            val source = method \\ "SourceLine" head
            val filename = source.attribute("sourcepath").get.head.text
            val lineNumber = {
              if (source.attribute("start").isDefined) {
                ":" + source.attribute("start").get.head.text +
                  "-" + source.attribute("end").get.head.text
              } else {
                ""
              }
            }
            "\nMethod " + method.attribute("name").get.head.text + " in class " +
              method.attribute("classname").get.head.text + " found in " + filename +
              lineNumber + ": signature " + signature + " static " + isStatic
          }).getOrElse("")
      }

      log.error(bugType + " found in " + filename + ":" + lineNumber + ": " + errorMessage)
      1
    }).sum
  }
}
