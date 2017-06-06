name := "skills"

version := "1.0"

scalaVersion := "2.12.2"

val http4sVersion = "0.17.0-M3"
val reactiveMongoVersion = "0.12.3"
val playVer = "2.6.1"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "reactivemongo" % reactiveMongoVersion,
  "org.reactivemongo" %% "reactivemongo-iteratees" % reactiveMongoVersion,
  "com.typesafe.play" %% "play-iteratees" % playVer,
  "org.http4s"     %% "http4s-dsl"            % http4sVersion,
  "org.http4s"     %% "http4s-blaze-server"   % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "io.circe" %% "circe-generic" % "0.6.1",
  "io.circe" %% "circe-literal" % "0.6.1",
  "ch.qos.logback" % "logback-classic"        % "1.1.3",
  "com.nimbusds" % "nimbus-jose-jwt" % "4.38"
)

assemblyExcludedJars in assembly := {
  val cp = (fullClasspath in assembly).value

  cp filter { af =>
    val file = af.data
    (file.getName == "scala-library-" + scalaVersion.value + ".jar") &&
      (file.getPath contains "org.scala-lang")
  }
}

assemblyJarName in assembly := "skills-" + version.value + ".jar"