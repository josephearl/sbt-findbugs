name := "findbugs-test-auto"

organization := "uk.co.josephearl"

version := "2.2.0"

(findbugs in Test) <<= (findbugs in Test) triggeredBy (compile in Test)
