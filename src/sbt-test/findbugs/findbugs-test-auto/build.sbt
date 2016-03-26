import com.lenioapp.sbt.findbugs._

name := "findbugs-test-auto"

organization := "com.lenioapp"

version := "2.0.0"

(FindBugs.findbugs in Test) <<= (FindBugs.findbugs in Test) triggeredBy (compile in Test)
