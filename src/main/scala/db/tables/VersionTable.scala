package db.tables
import db.Table
import doobie.free.connection.ConnectionIO
import doobie.util.log
import doobie.implicits._

object VersionTable extends Table {
	def drop(implicit h: log.LogHandler): ConnectionIO[Int] =
		sql"""
			DROP TABLE IF EXISTS version
		""".update.run

	def create(implicit h: log.LogHandler): ConnectionIO[Int] =
		sql"""
			CREATE TABLE version (
				num MEDIUMINT UNSIGNED NOT NULL
			)
		""".update.run
}
