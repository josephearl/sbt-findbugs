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

object FindBugsPriority extends Enumeration {
  type FindBugsPriority = Value

  val Relaxed = Value("-relaxed")
  val Low = Value("-low")
  val Medium = Value("-medium")
  val High = Value("-high")
}
