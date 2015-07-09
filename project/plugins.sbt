logLevel := Level.Warn


resolvers := Seq(
  "Public Repository" at "http://a360nexus.autodesk.com/nexus/content/groups/public/",
  "typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/",
  "Autodesk Nexus Snapshots" at "http://a360nexus.autodesk.com/nexus/content/repositories/snapshots"
)

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.6")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")