package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// Todoを表すモデル
//~~~~~~~~~~~~~~~~~~~~
case class Todo(
  id:         Option[Todo.Id],
  categoryId: Category.Id,
  title:      String,
  body:       String,
  state:      Todo.Status,
  updatedAt:  LocalDateTime = NOW,
  createdAt:  LocalDateTime = NOW
) extends EntityModel[Todo.Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Todo {

  val  Id = the[Identity[Id]]
  type Id = Long @@ Todo
  type WithNoId   = Entity.WithNoId  [Id, Todo]
  type EmbeddedId = Entity.EmbeddedId[Id, Todo]

  // ステータス定義: code は init.sql の state(0/1/2) に一致させる
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {
    case object TODO        extends Status(code = 0, name = "着手前")
    case object IN_PROGRESS extends Status(code = 1, name = "進行中")
    case object COMPLETED   extends Status(code = 2, name = "完了")
  }
}
