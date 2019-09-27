/*
 * Copyright (c) 2015-2016 Joseph Earl & contributors.
 * All rights reserved.
 *
 * Copyright (c) 2010-2014 Joachim Hofer & contributors
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse
 * Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package uk.co.josephearl.sbt.findbugs

import java.io.File
import javax.xml.transform.stream.StreamSource
import net.sf.saxon.s9api.Processor
import sbt._
import sbt.Keys._
import FindBugsReportType._

object FindBugs extends Object with CommandLine with CommandLineExecutor {
  def findbugs(findbugsClasspath: Classpath, compileClasspath: Classpath,
      paths: PathSettings, filters: FilterSettings, misc: MiscSettings, javaHome: Option[File],
      streams: TaskStreams): Unit = {
    val log = streams.log

    IO.withTemporaryDirectory { filterPath =>
      val cmd = commandLine(findbugsClasspath, compileClasspath, paths, filters, filterPath, misc, streams)
      log.debug("SpotBugs command line to execute: \"%s\"" format (cmd mkString " "))
      executeCommandLine(cmd, javaHome, log)
    }

    if (paths.reportPath.exists(f => f.exists())) {
      misc.xsltTransformations match {
        case None => // Nothing to do
        case Some(xslt) =>
          checkReportTypeXml(log, "`xsltTransformations := Some[Set[FindBugsXSLTTransformation]]`", misc.reportType)
          applyXSLT(paths.reportPath.get, xslt)
      }

      if (misc.failOnError) {
        checkReportTypeXml(log, "`findbugsFailOnError := true`", misc.reportType)

        val issuesFound = paths.reportPath
          .map(f => processIssues(log, f.getAbsolutePath))
          .sum

        if (issuesFound > 0) {
          log.error(issuesFound + " issue(s) found in SpotBugs report: " + paths.reportPath.get + "")
          sys.exit(1)
        }
      }
    }
  }

  def checkReportTypeXml(log: Logger, message: String, reportType: Option[FindBugsReportType]): Unit = {
    reportType match {
      case Some(FindBugsReportType.Xml) =>
      case Some(FindBugsReportType.XmlWithMessages) =>
      case _ =>
        log.error(s"$message can only be used in combination with `findbugsReportType := Some(FindBugsReportType.Xml)`" +
          s" or `Some(FindBugsReportType.XmlWithMessages)`")
        sys.exit(1)
    }
  }

  private[findbugs] def processIssues(log: Logger, outputLocation: String): Int = {
    val report = scala.xml.XML.loadFile(file(outputLocation))
    val warnings = report \\ "BugCollection" \\ "BugInstance"

    warnings.map(bug => {
      val bugType = bug.attribute("type").get.head.text
      val source = bug \\ "SourceLine" head
      val filename = source.attribute("sourcepath").get.head.text
      val lineNumber = source.attribute("start").map(_.head.text)

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

      log.error(bugType + " found in " + filename + lineNumber.map(":" + _).getOrElse("") + ": " + errorMessage)
      1
    }).sum
  }

  private def applyXSLT(input: File, transformations: Set[FindBugsXSLTTransformation]): Unit = {
    val processor = new Processor(false)
    val source = processor.newDocumentBuilder().build(input)

    transformations foreach { transform: FindBugsXSLTTransformation =>
      val output = processor.newSerializer(transform.output)
      val compiler = processor.newXsltCompiler()
      val executor = compiler.compile(new StreamSource(transform.xslt))
      val transformer = executor.load()
      transformer.setInitialContextNode(source)
      transformer.setDestination(output)
      transformer.transform()
      transformer.close()
      output.close()
    }
  }
}
