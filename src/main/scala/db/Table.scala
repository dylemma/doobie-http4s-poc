package db

import cats.free.Free
import doobie.free.connection.ConnectionIO
import doobie.util.log.LogHandler

trait Table {
	def drop(implicit h: LogHandler): ConnectionIO[Int]
	def create(implicit h: LogHandler): ConnectionIO[Int]
	def addForeignKeys(implicit h: LogHandler): ConnectionIO[Int] = Free.pure(0)
}
