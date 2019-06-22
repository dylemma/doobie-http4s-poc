import cats.effect._
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.hikari._

object HikariApp extends IOApp {

	// Resource yielding a transactor configured with a bounded connect EC and an unbounded
	// transaction EC. Everything will be closed and shut down cleanly after use.
	val transactor: Resource[IO, HikariTransactor[IO]] =
	for {
		ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
		te <- ExecutionContexts.cachedThreadPool[IO]    // our transaction EC
		xa <- HikariTransactor.newHikariTransactor[IO](
			"com.mysql.jdbc.Driver",                        // driver classname
			"jdbc:mysql://localhost/doobie_test",   // connect URL
			"codedx",                                   // username
			"codedx",                                     // password
			ce,                                     // await connection here
			te                                      // execute JDBC operations here
		)
	} yield xa


	def run(args: List[String]): IO[ExitCode] =
		transactor.use { xa =>

			// Construct and run your server here!
			for {
				i <- db.init.transact(xa)
				_ <- IO(println(s"Tables init affected $i rows?"))
				n <- sql"select 42".query[Int].unique.transact(xa)
				_ <- IO(println(n))
			} yield ExitCode.Success
		}

}