import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._

object SampleBuild extends Build {
  import Dependencies._

  lazy val root = project.in(file("."))
    .aggregate(js, jvm, client, server)
    .settings(sharedSettings(): _*)
    .settings(publishArtifact := false)
  lazy val core = crossProject.crossType(CrossType.Pure).in(file("core"))
    .settings(withCompatUnmanagedSources(jsJvmCrossProject = true, include_210Dir = false, includeTestSrcs = true): _*)
    .settings(sharedSettings(): _*)
    .settings(
      autoAPIMappings := true,
      apiMappings += (scalaInstance.value.libraryJar -> url(s"http://www.scala-lang.org/api/${scalaVersion.value}/"))
    )
    .jsSettings(
      libraryDependencies ++= Seq(
        scalaJs.dom.js,
        scribe.core.js,
        upickle.core.js,
        autowire.core.js,
        scalaTest.core.js % "test"
      ),
      scalaJSStage in Global := FastOptStage
    )
    .jvmSettings(
      javaOptions += "-verbose:gc",
      libraryDependencies ++= Seq(
        scribe.slf4j.jvm,
        upickle.core.jvm,
        autowire.core.jvm,
        spray.can,
        spray.routing,
        akka.actor,
        scalaTest.core.jvm % "test"
      )
    )
  lazy val js = core.js
  lazy val jvm = core.jvm

  // Platforms
  lazy val server: Project = project.in(file("server"))
    .settings(sharedSettings(Some("server")))
    .dependsOn(jvm)
  lazy val client: Project = project.in(file("client"))
    .settings(sharedSettings(Some("client")))
    .settings(
      crossTarget in fastOptJS := baseDirectory.value / ".." / "content" / "app",
      crossTarget in fullOptJS := baseDirectory.value / ".." / "content" / "app",
      crossTarget in packageJSDependencies := baseDirectory.value / ".." / "content" / "app"
    )
    .enablePlugins(ScalaJSPlugin)
    .dependsOn(js)

  def sharedSettings(projectName: Option[String] = None) = Seq(
    name := s"${Details.name}${projectName.map(pn => s"-$pn").getOrElse("")}",
    version := Details.version,
    organization := Details.organization,
    scalaVersion := Details.scalaVersion,
    sbtVersion := Details.sbtVersion,
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("snapshots"),
      Resolver.sonatypeRepo("releases"),
      Resolver.typesafeRepo("releases")
    ),
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-reflect" % _),
    publishArtifact in Test := false
  )

  /**
    * Helper function to add unmanaged source compat directories for different scala versions
    */
  private def withCompatUnmanagedSources(jsJvmCrossProject: Boolean, include_210Dir: Boolean, includeTestSrcs: Boolean): Seq[Setting[_]] = {
    def compatDirs(projectbase: File, scalaVersion: String, isMain: Boolean) = {
      val base = if (jsJvmCrossProject ) projectbase / ".." else projectbase
      CrossVersion.partialVersion(scalaVersion) match {
        case Some((2, scalaMajor)) if scalaMajor >= 11 => Seq(base / "compat" / "src" / (if (isMain) "main" else "test") / "scala-2.11").map(_.getCanonicalFile)
        case Some((2, scalaMajor)) if scalaMajor == 10 && include_210Dir => Seq(base / "compat" / "src" / (if (isMain) "main" else "test") / "scala-2.10").map(_.getCanonicalFile)
        case _ => Nil
      }
    }
    val unmanagedMainDirsSetting = Seq(
      unmanagedSourceDirectories in Compile ++= {
        compatDirs(projectbase = baseDirectory.value, scalaVersion = scalaVersion.value, isMain = true)
      }
    )
    if (includeTestSrcs) {
      unmanagedMainDirsSetting ++ {
        unmanagedSourceDirectories in Test ++= {
          compatDirs(projectbase = baseDirectory.value, scalaVersion = scalaVersion.value, isMain = false)
        }
      }
    } else {
      unmanagedMainDirsSetting
    }
  }
}

object Details {
  val organization = "com.outr"
  val name = "sample"
  val version = "1.0.0-SNAPSHOT"

  val sbtVersion = "0.13.11"
  val scalaVersion = "2.11.8"
}

object Dependencies {
  object upickle extends Dependencies("com.lihaoyi", "0.4.1") {
    val core = dep("upickle")
  }
  object autowire extends Dependencies("com.lihaoyi", "0.2.5") {
    val core = dep("autowire")
  }
  object scalaJs extends Dependencies("org.scala-js", "0.9.0") {
    val dom = dep("scalajs-dom")
  }
  object scribe extends Dependencies("com.outr.scribe", "1.2.3") {
    val core = dep("scribe")
    val slf4j = dep("scribe-slf4j")
  }
  object spray extends Dependencies("io.spray", "1.3.3") {
    val can = artifact("spray-can")
    val routing = artifact("spray-routing")
  }
  object akka extends Dependencies("com.typesafe.akka", "2.4.7") {
    val actor = artifact("akka-actor")
  }
  object scalaTest extends Dependencies("org.scalatest", "3.0.0-M16-SNAP4") {
    val core = dep("scalatest")
  }
}

class Dependencies(group: String, version: String) {
  def artifact(name: String, scala: Boolean = true) = if (scala) {
    group %% name % version
  } else {
    group % name % version
  }

  def dep(name: String, scala: Boolean = true) = new Dependency(name, scala)

  class Dependency(name: String, scala: Boolean = true) {
    def js = group %%%! name % version
    def jvm = group %% name % version
  }
}