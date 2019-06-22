package db.tables

import db.Table
import doobie.implicits._
import doobie.util.log.LogHandler

object ProjectTable extends Table {

	def drop(implicit h: LogHandler) =
		sql"""
			DROP TABLE IF EXISTS projects
		""".update.run

	def create(implicit h: LogHandler) =
		sql"""
			CREATE TABLE projects (
				id MEDIUMINT NOT NULL AUTO_INCREMENT,
				name TEXT NOT NULL,
				PRIMARY KEY (id)
			);
		""".update.run
}
