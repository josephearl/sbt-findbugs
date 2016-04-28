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

sealed abstract class FindBugsPriority

object FindBugsPriority {
  case object Relaxed extends FindBugsPriority {
    override def toString = "-relaxed"
  }

  case object Low extends FindBugsPriority {
    override def toString = "-low"
  }

  case object Medium extends FindBugsPriority {
    override def toString = "-medium"
  }

  case object High extends FindBugsPriority {
    override def toString = "-high"
  }
}

