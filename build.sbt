import org.scalastyle.sbt.ScalastylePlugin
import sbt._

name := "tct-challenger"

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

lazy val appResolvers = Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Autodesk Nexus Snapshots" at "http://a360nexus.autodesk.com/nexus/content/repositories/snapshots",
  "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Autodesk Nexus Public" at "http://a360nexus.autodesk.com/nexus/content/groups/public",
  "Public" at "http://repo1.maven.org/maven2/",
  "Sonatype repo" at "https://oss.sonatype.org/content/groups/scala-tools/",
  "maven kungfuters" at "http://maven.kungfuters.org/content/groups/public"
)

lazy val commonSettings = Seq(
  organization := "com.autodesk",
  organizationName := "Autodesk",
  organizationHomepage := Some(url("http://www.autodesk.com")),
  version := "1.0",
  scalaVersion := "2.10.4",
  scalacOptions ++= Seq("-encoding", "UTF-8", "-deprecation", "-unchecked"),
  resolvers ++= appResolvers,
  compileScalastyle := scalastyle.in(Compile).toTask("").value,
  (compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle
) ++ Seq(ScalastylePlugin.projectSettings: _*)

lazy val challengerCommon = project.in(file("challenger-common"))
  .settings(
    commonSettings
  ).settings(
    libraryDependencies ++= commonDependencies
  )

lazy val challengerCassandra = project.in(file("challenger-cassandra"))
  .aggregate(challengerCommon)
  .dependsOn(challengerCommon)
  .settings(
    commonSettings
  ).settings(
    libraryDependencies ++= challengerCassandraDependencies
  )


lazy val root = project.in(file(".")).enablePlugins(PlayScala)
  .dependsOn(challengerCommon, challengerCassandra)
  .aggregate(challengerCommon, challengerCassandra)
  .settings(
    commonSettings
  ).settings(
    libraryDependencies ++= rootDependencies
  )

sourceDirectories in (Compile, TwirlKeys.compileTemplates) := (unmanagedSourceDirectories in Compile).value

lazy val rootDependencies = Seq(
  "com.typesafe.play" %% "play" % "2.3.6" exclude("com.typesafe.play","twirl"),
  "org.webjars" % "bootstrap" % "2.3.1",
  "com.sksamuel.scrimage" %% "scrimage-core" % "1.4.2",
  "com.sksamuel.scrimage" %% "scrimage-canvas" % "1.4.2",
  "com.sksamuel.scrimage" %% "scrimage-filters" % "1.4.2",
  "com.notnoop.apns" % "apns" % "0.2.3"

  //add extra dependencies here
)
lazy val commonDependencies = Seq(
  "joda-time" % "joda-time" % "2.3"
)

lazy val challengerCassandraDependencies = Seq(
  "org.apache.cassandra" % "cassandra-all" % "2.1.2",
  "com.websudos" %% "phantom-dsl" % "1.4.0"
)


fork in run := true