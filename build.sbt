scalacOptions += "-Ypartial-unification" // 2.11.9+

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
	"mysql" % "mysql-connector-java" % "5.1.24"
)