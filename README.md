# sbt-findbugs-plugin

An SBT 0.13+ plugin for running FindBugs on Java classes. For more information about FindBugs, see <http://findbugs.sourceforge.net>.

This plugin currently uses version 3.0.0 of FindBugs.

## Getting started

Add sbt-findbugs-plugin as a plugin in your project's `project/plugins.sbt`:

```scala
addSbtPlugin("com.lenioapp" % "sbt-findbugs-plugin" % "2.0.0")
```

sbt-findbugs-plugin is an AutoPlugin, so there is no need to modify the `build.sbt` file to enable it.

If you want to modify any of the default settings, you should add the following import to build.sbt, however:

```scala
import com.lenioapp.sbt.findbugs._
```

## Defining exclude/include filters

### Defining filters inline

Just use Scala inline XML for the setting, for example:

```scala
FindBugs.includeFilters := Some(<FindBugsFilter>
  <Match>
    <Class name="de.johoop.Meep" />
  </Match>
</FindBugsFilter>)
```

### Using filter files

You can also read the filter settings from files in a more conventional way:

```scala
FindBugs.includeFilters := Some(baseDirectory.value / "findbugs-include-filters.xml")
```

Or, when your configuration is zipped and previously published to a local repo:

```scala
FindBugs.includeFilters := {
  val configFiles = update.value.select(module = moduleFilter(name = "velvetant-sonar"))
  val configFile = configFiles.headOption flatMap { zippedFile =>
    IO.unzip(zippedFile, target.value / "rules") find (_.name contains "velvetant-sonar-findbugs.xml")
  }

  configFile map scala.xml.XML.loadFile orElse sys.error("unable to find config file in update report")
}
```

## Settings

(see also the [FindBugs documentation](http://findbugs.sourceforge.net/manual/running.html#commandLineOptions))

### `reportType`
* *Description:* Optionally selects the output format for the FindBugs report.
* *Accepts:* `Some(ReportType.{Xml, Html, PlainHtml, FancyHtml, FancyHistHtml, Emacs, Xdoc})`
* *Default:* `Some(ReportType.Xml)`

### `reportPath`
* *Description:* Target path of the report file to generate (optional).
* *Accepts:* any legal file path
* *Default:* `Some(crossTarget.value / "findbugs" / "report.xml")`

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

### `plugins`
* *Description:* A list of FindBugs plugin jars enable.
* *Accepts:* any `Seq[File]`
* *Default:* `Seq()`
