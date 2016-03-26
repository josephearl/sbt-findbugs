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

sealed abstract class FindBugsEffort

object FindBugsEffort {

  case object Minimum extends FindBugsEffort {
    override def toString = "min"
  }

  case object Default extends FindBugsEffort {
    override def toString = "default"
  }

  case object Maximum extends FindBugsEffort {
    override def toString = "max"
  }

}

