scalacOptions ++= Seq(
	"-deprecation",
	"-encoding", "UTF-8",
	"-language:higherKinds",
	"-language:postfixOps",
	"-feature",
	"-Ypartial-unification",
	"-Xfatal-warnings",
)

val Http4sVersion = "0.20.3"
val CirceVersion = "0.11.1"
val Specs2Version = "4.1.0"
val LogbackVersion = "1.2.3"

libraryDependencies ++= Seq(

	// Start with this one
	"org.tpolecat" %% "doobie-core"      % "0.7.0",

	// And add any of these as needed
	"org.tpolecat" %% "doobie-hikari"    % "0.7.0",          // HikariCP transactor.
	"org.tpolecat" %% "doobie-scalatest" % "0.7.0" % "test",  // ScalaTest support for typechecking statements.

	// Cats
	"org.typelevel" %% "cats-core" % "1.6.0",
	"org.typelevel" %% "cats-kernel" % "1.6.0",
	"org.typelevel" %% "cats-effect" % "1.2.0",

	// Monix
	"io.monix" %% "monix" % "3.0.0-RC2",

	// Mysql driver
	"mysql" % "mysql-connector-java" % "5.1.24",

	"org.http4s"      %% "http4s-blaze-server" % Http4sVersion,
	"org.http4s"      %% "http4s-blaze-client" % Http4sVersion,
	"org.http4s"      %% "http4s-circe"        % Http4sVersion,
	"org.http4s"      %% "http4s-dsl"          % Http4sVersion,
	"io.circe"        %% "circe-generic"       % CirceVersion,
	"org.specs2"      %% "specs2-core"         % Specs2Version % "test",
	"ch.qos.logback"  %  "logback-classic"     % LogbackVersion
)

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
addCompilerPlugin("com.olegpy"    %% "better-monadic-for" % "0.3.0")