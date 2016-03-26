# sbt-findbugs-plugin [![Build Status](https://travis-ci.org/lenioapp/sbt-findbugs-plugin.svg?branch=master)](https://travis-ci.org/lenioapp/sbt-findbugs-plugin)

An SBT 0.13+ plugin for running FindBugs on Java classes. For more information about FindBugs, see <http://findbugs.sourceforge.net>.

This plugin currently uses FindBugs version 3.0.1.

## Getting started

Add sbt-findbugs-plugin as a plugin in your projects `project/plugins.sbt`:

```scala
addSbtPlugin("com.lenioapp" % "sbt-findbugs-plugin" % "2.0.0")
```

sbt-findbugs-plugin is an AutoPlugin, so there is no need to modify the `build.sbt` file to enable it.

If you want to modify any of the default settings, you should add the following import to build.sbt, however:

```scala
import com.lenioapp.sbt.findbugs._
```

## Usage

You can run FindBugs over your Java classes with the `findbugs` task. You can run FindBugs over your Java test classes with the `test:findbugs` task.

The FindBugs report is output to `target/findbugs-report.xml` by default. This can be changed by setting the value of `FindBugs.outputFile`. By default `test:findbugs` outputs to `target/findbugs-test-report.xml`, this can be changed by setting the value of `FindBugs.outputFile in Test`.

You can define include/exclude filters either inline in the `build.sbt` or in an external XML file.

### Defining filters inline

Just use Scala inline XML for the setting, for example:

```scala
FindBugs.includeFilters := Some(<FindBugsFilter>
  <Match>
    <Class name="com.lenioapp.example.Example" />
  </Match>
</FindBugsFilter>)
```

### Defining filters using filter files

You can also read the filter settings from files in a more conventional way:

```scala
FindBugs.includeFilters := Some(baseDirectory.value / "findbugs-include-filters.xml")
```

Or, if your configuration is zipped and previously published to a local repo:

```scala
FindBugs.includeFilters := {
  val configFiles = update.value.select(module = moduleFilter(name = "velvetant-sonar"))
  val configFile = configFiles.headOption flatMap { zippedFile =>
    IO.unzip(zippedFile, target.value / "rules") find (_.name contains "velvetant-sonar-findbugs.xml")
  }

  configFile map scala.xml.XML.loadFile orElse sys.error("unable to find config file in update report")
}
```

### Using FindBugs plugins

To use FindBugs plugins such as [fb-contrib](http://fb-contrib.sourceforge.net) or [find-sec-bugs](http://find-sec-bugs.github.io) use the `pluginList` setting:

```scala
libraryDependencies += "com.mebigfatguy.fb-contrib" % "fb-contrib" % "6.6.0"

FindBugs.pluginList += s"${ivyPaths.value.ivyHome.get.absolutePath}/cache/com.mebigfatguy.fb-contrib/fb-contrib/jars/fb-contrib-6.6.0.jar"
```

Or download the plugins to your projects `lib` directory:

```scala
FindBugs.pluginList += file("lib/fb-contrib-6.6.0.jar").absolutePath
```

### Running FindBugs automatically

To run FindBugs automatically after compilation add the following to your `build.sbt`:

```scala
(FindBugs.findbugs in Compile) <<= (FindBugs.findbugs in Compile) triggeredBy (compile in Compile)
```

To run FindBugs automatically after test compilation:

```scala
(FindBugs.findbugs in Test) <<= (FindBugs.findbugs in Test) triggeredBy (compile in Test)
```

### Failing the build

You can set FindBugs to fail the build if any bugs are found by setting `failOnError`:

```scala
FindBugs.failOnError := true
```

## Settings

### `reportType`
* *Description:* Optionally selects the output format for the FindBugs report.
* *Accepts:* `Some(ReportType.{Xml, Html, PlainHtml, FancyHtml, FancyHistHtml, Emacs, Xdoc})`
* *Default:* `Some(ReportType.Xml)`

### `outputPath`
* *Description:* Target path of the report file to generate (optional).
* *Accepts:* any legal file path
* *Default:* `Some(target.value / "findbugs" / "report.xml")`

### `priority`
* *Description:* Suppress reporting of bugs based on priority.
* *Accepts:* `Priority.{Relaxed, Low, Medium, High}`
* *Default:* `Priority.Medium`

### `effort`
* *Description:* Decide how much effort to put into analysis.
* *Accepts:* `Effort.{Minimum, Default, Maximum}`
* *Default:* `Effort.Default`

### `onlyAnalyze`
* *Description:* Optionally, define which packages/classes should be analyzed.
* *Accepts:* An option containing a `List[String]` of packages and classes.
* *Default:* `None` (meaning: analyze everything).

### `maxMemory`
* *Description:* Maximum amount of memory to allow for FindBugs (in MB).
* *Accepts:* any reasonable amount of memory as an integer value
* *Default:* `1024`

### `analyzeNestedArchives`
* *Description:* Whether FindBugs should analyze nested archives or not.
* *Accepts:* `true` and `false`
* *Default:* `true`

### `sortReportByClassNames`
* *Description:* Whether the reported bug instances should be sorted by class name or not.
* *Accepts:* `true` and `false`
* *Default:* `false`

### `failOnError`
* *Description:* Whether the build should be failed if there are any reported bug instances.
* *Accepts:* `true` and `false`
* *Default:* `false`

### `includeFilters`
* *Description:* Optional filter file XML content defining which bug instances to include in the static analysis.
* *Accepts:* `None` and `Option[Node]`
* *Default:* `None` (no include filters).

### `excludeFilters`
* *Description:* Optional filter file XML content defining which bug instances to exclude in the static analysis.
* *Accepts:* `None` and `Some[Node]`
* *Default:* `None` (no exclude filters).

### `analyzedPath`
* *Description:* The path to the classes to be analyzed.
* *Accepts:* any `sbt.Path`
* *Default:* `Seq(classDirectory in Compile value)`

### `pluginList`
* *Description:* A list of FindBugs plugins to enable, can be an absolute path to a plugin or the name of a plugin in the FindBugs optional plugins directory `~/.findbugs/optionalPlugin`.
* *Accepts:* any `Seq[String]`
* *Default:* `Seq()`
