package db

import cats.effect._
import doobie._
import doobie.hikari.HikariTransactor
import doobie.util.transactor.{ Strategy, Transactor }

// database instance
trait DBI[F[_]] {
	def tx: Transactor[F]
	def readOnly: Transactor[F]
}

object DBI {
	def apply[F[_]](txInstance: Transactor[F], readOnlyInstance: Transactor[F]): DBI[F] = new DBI[F] {
		val tx = txInstance
		val readOnly = readOnlyInstance
	}

	def mysqlHikari[F[_] : Sync : Async : ContextShift](user: String, pass: String, dbHostAndPath: String): Resource[F, DBI[F]] = {
		for {
			ce <- ExecutionContexts.fixedThreadPool[F](32) // our connect EC
			te <- ExecutionContexts.cachedThreadPool[F] // our transaction EC
			tx <- HikariTransactor.newHikariTransactor[F](
				"com.mysql.jdbc.Driver", // driver classname
				s"jdbc:mysql://$dbHostAndPath", // connect URL
				user, // username
				pass, // password
				ce, // await connection here
				te // execute JDBC operations here
			)
		} yield {
			val readOnly = Transactor.strategy.set(tx, Strategy(FC.setReadOnly(true), FC.unit, FC.unit, FC.unit))
			apply(tx, readOnly)
		}
	}
}
