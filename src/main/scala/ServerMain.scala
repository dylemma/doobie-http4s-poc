import api.DemoServer
import cats.effect.{ ExitCode, IO, IOApp }
import cats.implicits._

object ServerMain extends IOApp {
	def run(args: List[String]) =
		DemoServer.stream[IO].compile.drain.as(ExitCode.Success)
}