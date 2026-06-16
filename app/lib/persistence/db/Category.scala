package lib.persistence.db

import java.time.LocalDateTime
import ixias.slick.jdbc.MySQLProfile.api._
import ixias.slick.builder._
import lib.model.Category

// CategoryTable: to_do_category テーブルへのマッピングを行う
//~~~~~~~~~~~~~~
case class CategoryTable(tag: Tag) extends Table[Category](tag, "to_do_category") {
    // Columns
    /* @1 */ def id        = column[Category.Id]    ("id",         UInt64, O.PrimaryKey, O.AutoInc)
    /* @2 */ def name      = column[String]         ("name",       Utf8Char255)
    /* @3 */ def slug      = column[String]         ("slug",       AsciiChar64)
    /* @4 */ def color     = column[Category.Color] ("color",      UInt8)
    /* @5 */ def updatedAt = column[LocalDateTime]  ("updated_at", TsCurrent)
    /* @6 */ def createdAt = column[LocalDateTime]  ("created_at", Ts)

    // DB <=> Scala の相互のmapping定義
    def * = (id.?, name, slug, color, updatedAt, createdAt).<> (
      (Category.apply _).tupled,
      (Category.unapply _).andThen(_.map(_.copy(
        _5 = LocalDateTime.now()
      )))
    )
}
