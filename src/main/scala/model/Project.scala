package model

import cats.Applicative
import db.DBAction
import doobie.implicits._
import doobie.util.{ Read, Write }
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.http4s.EntityEncoder
import org.http4s.circe._

case class Project(id: Int, name: String)

object Project extends ((Int, String) => Project) {
	implicit val read: Read[Project] = Read[(Int, String)].map(Project.tupled)
	implicit val write: Write[Project] = Write[(Int, String)].contramap(Project.unapply(_).get)
	implicit val encoder: Encoder[Project] = deriveEncoder[Project]
	implicit def entityEncoder[F[_] : Applicative]: EntityEncoder[F, Project] = jsonEncoderOf[F, Project]
}

class ProjectAccess {
	def create(name: String) = DBAction.tx[Project] {
		sql"insert into projects (name) values ($name)"
			.update
			.withUniqueGeneratedKeys[Int]("id")
			.map(Project(_, name))
	}

	def listAll = DBAction.readOnly[List[Project]] {
		sql"select id, name from projects"
			.query[Project]
			.to[List]
	}

	def findById(id: Int) = DBAction.readOnly[Option[Project]] {
		sql"select id, name from projects where id = $id"
			.query[Project]
			.option
	}

	def deleteById(id: Int) = DBAction.tx {
		sql"delete from projects where id = $id"
			.update
			.run
			.map(_ > 0)
	}
}
