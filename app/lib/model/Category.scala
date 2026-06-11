/**
  * This is a sample of Todo Application.
  *
  */

package lib.model

import ixias.model._
import ixias.util.EnumStatus

import java.time.LocalDateTime

// カテゴリーを表すモデル
//~~~~~~~~~~~~~~~~~~~~
case class Category(
  id:        Option[Category.Id],
  name:      String,
  slug:      String,
  color:     Category.Color,
  updatedAt: LocalDateTime = NOW,
  createdAt: LocalDateTime = NOW
) extends EntityModel[Category.Id]

// コンパニオンオブジェクト
//~~~~~~~~~~~~~~~~~~~~~~~~
object Category {

  val  Id = the[Identity[Id]]
  type Id = Long @@ Category
  type WithNoId   = Entity.WithNoId  [Id, Category]
  type EmbeddedId = Entity.EmbeddedId[Id, Category]

  // カラー定義: code を DB に保存し、rgb を画面で利用する
  //~~~~~~~~~~~~~~~~~
  sealed abstract class Color(val code: Short, val name: String, val rgb: String) extends EnumStatus
  object Color extends EnumStatus.Of[Color] {
    case object RED   extends Color(code = 1, name = "レッド", rgb = "#e74c3c")
    case object GREEN extends Color(code = 2, name = "グリーン", rgb = "#2ecc71")
    case object BLUE  extends Color(code = 3, name = "ブルー", rgb = "#3498db")
  }
}
