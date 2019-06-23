package api

import cats.Applicative
import cats.effect.Sync
import db.DBI
import model.{ Project, ProjectAccess }
import cats.implicits._
import io.circe.Encoder
import org.http4s.{ EntityEncoder, HttpRoutes }
import org.http4s.dsl.Http4sDsl
import org.http4s.circe._
import io.circe.generic.semiauto._

object ProjectRoutes {

	case class CreateProjectRequest(name: String)

	def apply[F[_]: Sync : Applicative](projects: handlers.Projects[F]) = {
		val dsl = new Http4sDsl[F]{}
		import dsl._

		implicit def responseEntityEncoder[A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf
		implicit val decodeCreateProjectRequest = jsonOf[F, CreateProjectRequest](implicitly, deriveDecoder)

		HttpRoutes.of[F] {
			// get a list of all projects
			case GET -> Root / "projects" =>
				for {
					allProjects <- projects.listAll
					resp <- Ok(allProjects)
				} yield resp

			// get an individual project
			case GET -> Root / "projects" / IntVar(id) =>
				for {
					projectOpt <- projects.findById(id)
					resp <- projectOpt match {
						case None => NotFound()
						case Some(p) => Ok(p)
					}
				} yield resp

			// create a new project
			case req @ POST -> Root / "projects" =>
				for {
					toCreate <- req.as[CreateProjectRequest]
					project <- projects.create(toCreate.name)
					resp <- Ok(project)
				} yield resp

			// delete a project
			case DELETE -> Root / "projects" / IntVar(id) =>
				for {
					didDelete <- projects.deleteById(id)
					resp <- if(didDelete) Ok() else NotFound()
				} yield resp
		}
	}
}
