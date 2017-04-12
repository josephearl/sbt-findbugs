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

/**
  * Confidence level of a bug, the likelihood that FindBugs has flagged a real bug.
  */
object FindBugsConfidence extends Enumeration {
  type FindBugsConfidence = Value

  /** Report warnings of any confidence level. **/
  val Low = Value("-low")
  /** Report only medium and high confidence warnings. This is the default confidence used. **/
  val Medium = Value("-medium")
  /** Report only high confidence warnings. **/
  val High = Value("-high")
}
