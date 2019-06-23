import cats.data._
import cats.free.Free
import cats.implicits._
import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException
import doobie._
import doobie.implicits._

import db.tables._

package object db {

	val latestVersion = 1

	// a list of all of the tables
	def all: List[Table] = List(
		ProjectTable,
		VersionTable,
	)

	// ensure the db is installed and updated
	def init(implicit h: LogHandler = LogHandler.nop): ConnectionIO[Unit] = {
		for {
			currentVersion <- findVersion
			_ <- ensureDbUpToDate(currentVersion)
		} yield ()
		// TODO: could use this to gate access to the Table instances
	}

	// discover the currently-installed version
	private def findVersion(implicit h: LogHandler) = {
		sql"select num from version"
			.query[Int]
			.unique
   		.recover {
				case e: MySQLSyntaxErrorException if e.getMessage matches missingVersionErrorMessage =>
					println(s"Looks like the database isn't set up yet. ")
					0
			}
	}
	private val missingVersionErrorMessage = "Table '.*version' doesn't exist"

	// decides to fresh install, update, or nop based on the `fromVersion`
	private def ensureDbUpToDate(fromVersion: Int)(implicit h: LogHandler): ConnectionIO[Unit] =
		if(fromVersion == 0) {
			println("Fresh install db")
			for {
				_ <- forceInitTables(h)
				_ <- sql"insert into version (num) values ($latestVersion)".update.run
			} yield ()
		} else if(fromVersion == latestVersion) {
			println("Db already installed!")
			FC.unit
		} else {
			println(s"Update db from version $fromVersion to $latestVersion (not supported right now)")
			???
	}

	// fresh install
	private def forceInitTables(implicit h: LogHandler) = {
		val createOps = all.map { table =>
			(table.drop, table.create).mapN(_ + _)
		}
		val foreignKeyOps = all.map { _.addForeignKeys }
		NonEmptyList.fromListUnsafe(createOps ++ foreignKeyOps).reduceMapM(identity)
	}
}
