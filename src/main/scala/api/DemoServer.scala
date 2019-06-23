package api

import scala.concurrent.ExecutionContext.global

import cats.effect.{ ConcurrentEffect, ContextShift, Timer }
import cats.implicits._
import db.DBI
import fs2.Stream
import model.ProjectAccess
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger

object DemoServer {

	def stream[F[_] : ConcurrentEffect](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {
		for {
			// single-element stream that `use`s the DBI resource and ensures the tables are set up
			dbi <- Stream.resource {
				DBI.mysqlHikari[F]("codedx", "codedx", "localhost/doobie_test").evalMap {
					DBI.ensureInstalled[F]
				}
			}

			// single-element stream that allocates (and deallocates when we're done) an HTTP client
			client <- BlazeClientBuilder[F](global).stream
			helloWorldAlg = handlers.HelloWorld.impl[F]
			jokeAlg = handlers.Jokes.impl[F](client)

			projectAccess = new ProjectAccess
			projectsAlg = handlers.Projects.impl(dbi, projectAccess)

			// Combine Service Routes into an HttpApp.
			// Can also be done via a Router if you
			// want to extract a segments not checked
			// in the underlying routes.
			httpApp = (
				DemoRoutes.helloWorldRoutes[F](helloWorldAlg) <+>
				DemoRoutes.jokeRoutes[F](jokeAlg) <+>
				ProjectRoutes[F](projectsAlg)
			).orNotFound

			// With Middlewares in place
			finalHttpApp = Logger.httpApp(true, true)(httpApp)

			exitCode <- BlazeServerBuilder[F]
				.bindHttp(8080, "0.0.0.0")
				.withHttpApp(finalHttpApp)
				.serve
		} yield exitCode
	}.drain
}