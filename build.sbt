import sbt.Keys._
import sbt._

name := """to-do-sample"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.13"

libraryDependencies += guice

resolvers ++= Seq(
  "Nextbeat Snapshots" at "https://s3-ap-northeast-1.amazonaws.com/maven.nextbeat.net/snapshots",
  "Nextbeat Releases"  at "https://s3-ap-northeast-1.amazonaws.com/maven.nextbeat.net/releases",
)

libraryDependencies ++= Seq(
  // まずはこの1つから
  "net.ixias"      %% "ixias"               % "2.1.1",
  "net.ixias"      %% "ixias-slick"         % "2.1.1", // Slickを使用したDBアクセス
  "mysql"          % "mysql-connector-java" % "8.0.33",
  "ch.qos.logback" % "logback-classic"      % "1.1.+",
)
// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
