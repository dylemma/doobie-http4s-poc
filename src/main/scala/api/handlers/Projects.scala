package api.handlers

import cats.Applicative
import cats.effect.Bracket
import db.DBI
import model.{ Project, ProjectAccess }

trait Projects[F[_]] {
	def findById(id: Int): F[Option[Project]]

	def listAll: F[List[Project]]

	def create(name: String): F[Project]

	def deleteById(id: Int): F[Boolean]
}
object Projects {
	def impl[F[_]](dbi: DBI[F], projects: ProjectAccess)(implicit bracket: Bracket[F, Throwable]): Projects[F] = new Projects[F] {
		def findById(id: Int): F[Option[Project]] = projects.findById(id).run(dbi)
		def listAll: F[List[Project]] = projects.listAll.run(dbi)
		def create(name: String): F[Project] = projects.create(name).run(dbi)
		def deleteById(id: Int): F[Boolean] = projects.deleteById(id).run(dbi)
	}
}
