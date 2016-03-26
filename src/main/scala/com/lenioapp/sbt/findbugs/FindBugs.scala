package com.lenioapp.sbt.findbugs

import java.io.File

import sbt.IO
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
}
