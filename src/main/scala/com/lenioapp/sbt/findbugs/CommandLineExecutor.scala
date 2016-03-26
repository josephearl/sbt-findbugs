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

import sbt._
import scala.util.control.NonFatal

private[findbugs] trait CommandLineExecutor {
  private[findbugs] def executeCommandLine(commandLine: List[String], javaHome: Option[File], log: Logger) = try {
    val exitValue = Fork.java(
    	ForkOptions(javaHome = javaHome, outputStrategy = Some(new LoggedOutput(log))), 
    	commandLine)
    if (exitValue != 0) sys.error("Nonzero exit value when attempting to call FindBugs: " + exitValue)
    
  } catch {
    case NonFatal(e) => sys.error("Exception while executing FindBugs: %s".format(e.getMessage))
  }
}
