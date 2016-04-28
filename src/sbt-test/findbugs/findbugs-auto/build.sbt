name := "findbugs-auto"

organization := "uk.co.josephearl"

version := "2.2.0"

(findbugs in Compile) <<= (findbugs in Compile) triggeredBy (compile in Compile)
