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

import uk.co.josephearl.sbt.findbugs.FindBugsConfidence._
import uk.co.josephearl.sbt.findbugs.FindBugsReportType._

import scala.xml.Node

case class PathSettings(reportPath: Option[File], analyzedPath: Seq[File], auxPath: Seq[File])

case class FilterSettings(includeFilters: Option[Node], excludeFilters: Option[Node])

case class MiscSettings(
                         reportType: Option[FindBugsReportType], confidence: FindBugsConfidence,
                         onlyAnalyze: Option[Seq[String]], maxMemory: Int,
                         analyzeNestedArchives: Boolean, sortReportByClassNames: Boolean,
                         effort: FindBugsEffort, failOnError: Boolean, pluginList: Seq[String],
                         xsltTransformations: Option[Set[FindBugsXSLTTransformation]])
