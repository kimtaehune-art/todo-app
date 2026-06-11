/**
  * This is a sample of Todo Application.
  * 
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// ユーザーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
case class User(
  id:        Option[User.Id],
  name:      String,
  age:       Short,
  state:     User.Status,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[User.Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object User {

  val  Id = the[Identity[Id]]
  type Id = Long @@ User
  type WithNoId = Entity.WithNoId [Id, User]
  type EmbeddedId = Entity.EmbeddedId[Id, User]

  // ステータス定義
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Status(val code: Short, val name: String) extends EnumStatus
  object Status extends EnumStatus.Of[Status] {
    case object IS_INACTIVE extends Status(code = 0,   name = "無効")
    case object IS_ACTIVE   extends Status(code = 100, name = "有効")
  }
}