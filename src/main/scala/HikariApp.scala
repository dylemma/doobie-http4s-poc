import cats.effect._
import db.DBI
import doobie.implicits._
import monix.eval.{ Task, TaskApp }

object HikariApp extends TaskApp {
	def run(args: List[String]): Task[ExitCode] =
		DBI.mysqlHikari[Task]("codedx", "codedx", "localhost/doobie_test") use { dbi =>
			val projects = new model.ProjectAccess

			// Construct and run your server here!
			for {
				i <- db.init.transact(dbi.tx)
				p1 <- projects.create("hello").run(dbi)
				_ <- Task(println(s"Inserted ${p1}"))
				allProjects <- projects.listAll.run(dbi)
				_ <- Task(println(s"All projects: $allProjects"))
				p1a <- projects.findById(p1.id).run(dbi)
				_ <- Task(println(s"Found ${p1a} with id=${p1.id}"))
			} yield ExitCode.Success
		}
}