name := "findbugs-test-auto"

organization := "com.lenioapp"

version := "2.1.0"

(findbugs in Test) <<= (findbugs in Test) triggeredBy (compile in Test)
