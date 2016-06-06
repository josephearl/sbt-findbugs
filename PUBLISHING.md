# Publish

sbt-findbugs is published to Bintray using [bintray-sbt](https://github.com/softprops/bintray-sbt) and synced to the [sbt-plugin-releases](https://bintray.com/sbt/sbt-plugin-releases) repository for general availability.

1. Tag the latest commit `git tag -a v1.2.3 -m "1.2.3"`
2. Package and publish the latest release `sbt clean publish`
3. Push the latest release to GitHub `git push origin master v1.2.3`
