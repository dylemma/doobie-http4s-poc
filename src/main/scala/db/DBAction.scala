package db

import cats.Monad
import cats.effect.Bracket
import doobie._
import doobie.implicits._

sealed trait DBLevel
object DBLevel {
	class ReadOnly private[DBLevel]() extends DBLevel
	class Transactional private[DBLevel]() extends ReadOnly

	trait Enact[Level] {
		def apply[F[_]](dbi: DBI[F]): Transactor[F]
	}
	implicit val enactReadOnly: Enact[ReadOnly] = new Enact[ReadOnly] {
		def apply[F[_]](dbi: DBI[F]) = dbi.readOnly
	}
	implicit val enactTransactional: Enact[Transactional] = new Enact[Transactional] {
		def apply[F[_]](dbi: DBI[F]) = dbi.tx
	}
}
import db.DBLevel._

class DBAction[A, Level](val program: ConnectionIO[A])(implicit enact: Enact[Level]) {
	def run[F[_]](dbi: DBI[F])(implicit bracket: Bracket[F, Throwable]): F[A] = program.transact(enact(dbi))
	def flatMap[B, L2 <: Level](getNext: A => DBAction[B, L2])(implicit enact2: Enact[L2]): DBAction[B, L2] = new DBAction[B, L2](program.flatMap(a => getNext(a).program))

}
object DBAction {
	def pure[A, Level](x: A)(implicit enact: Enact[Level]): DBAction[A, Level] = new DBAction(FC.pure(x))
	def readOnly[A](program: ConnectionIO[A]): DBAction[A, ReadOnly] = new DBAction[A, DBLevel.ReadOnly](program)
	def tx[A](program: ConnectionIO[A]): DBAction[A, Transactional] = new DBAction[A, DBLevel.Transactional](program)

	def catsMonadForDBAction[Level](implicit enact: DBLevel.Enact[Level]): Monad[DBAction[?, Level]] = new Monad[DBAction[?, Level]] {
		def pure[A](x: A): DBAction[A, Level] = new DBAction(FC.pure(x))
		def flatMap[A, B](fa: DBAction[A, Level])(f: A => DBAction[B, Level]): DBAction[B, Level] = fa.flatMap(f)
		def tailRecM[A, B](a: A)(f: A => DBAction[Either[A, B], Level]): DBAction[B, Level] = {
			val program = Monad[ConnectionIO].tailRecM(a)(f.andThen(_.program))
			new DBAction(program)
		}
	}
}
