name := "findbugs-auto"

organization := "com.lenioapp"

version := "2.2.0"

(findbugs in Compile) <<= (findbugs in Compile) triggeredBy (compile in Compile)
