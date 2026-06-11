/**
  * This is a sample of Todo Application.
  * 
  */

package lib.persistence.db

import java.time.LocalDateTime
import ixias.slick.jdbc.MySQLProfile.api._
import ixias.slick.builder._
import lib.model.User

import java.time

// UserTable: Userテーブルへのマッピングを行う
//~~~~~~~~~~~~~~
case class UserTable(tag: Tag) extends Table[User](tag, "user") {
    // Columns
    /* @1 */ def id        = column[User.Id]       ("id",         UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def name      = column[String]        ("name",       Utf8Char255)
    /* @3 */ def age       = column[Short]         ("age",        UInt8)
    /* @4 */ def state     = column[User.Status]   ("state",      UInt8)
    /* @5 */ def updatedAt = column[LocalDateTime] ("updated_at", TsCurrent)
    /* @6 */ def createdAt = column[LocalDateTime] ("created_at", Ts)

    // DB <=> Scala の相互のmapping定義
    def * = (id.?, name, age, state, updatedAt, createdAt).<> (
      (User.apply _).tupled,
      (User.unapply _).andThen(_.map(_.copy(
        _5 = LocalDateTime.now()
      )))
    )
}