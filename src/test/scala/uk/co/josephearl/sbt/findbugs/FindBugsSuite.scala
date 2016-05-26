package uk.co.josephearl.sbt.findbugs

import org.scalatest.junit.JUnitSuite
import org.junit.Test
import org.junit.Assert._
import sbt.ConsoleLogger

class FindBugsSuite extends JUnitSuite {
  @Test
  def testProcessIssues_1464263403082_SourceLineWithoutStart() = {
    val log = ConsoleLogger()
    val file = getClass.getResource("findbugs-report-1464263403082.xml").getFile

    val issuesFound = FindBugs.processIssues(log, file)

    assertTrue("Some issues were found", issuesFound > 0)
  }
}
