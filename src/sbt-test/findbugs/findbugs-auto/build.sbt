import com.lenioapp.sbt.findbugs._

name := "findbugs-auto"

organization := "com.lenioapp"

version := "2.0.0"

(FindBugs.findbugs in Compile) <<= (FindBugs.findbugs in Compile) triggeredBy (compile in Compile)
