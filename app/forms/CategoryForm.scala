/**
 *
 * to do sample project : Category 追加フォーム
 *
 */

package forms

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints

import lib.model.Category

// カテゴリー追加フォームの入力データ
case class CategoryFormData(
  name:  String,
  slug:  String,
  color: Short,
)

object CategoryForm {

  // name: 英数字 + 半角スペース + 日本語、改行禁止 (Todo の title と同じ許可文字)
  private val nameAllowed =
    "A-Za-z0-9 \\u3000-\\u303F\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF\\uFF00-\\uFFEF"
  private val namePattern = ("[" + nameAllowed + "]+").r

  // slug: 英数字のみ (日本語・記号・改行すべて不可)
  private val slugPattern = "[A-Za-z0-9]+".r

  val form: Form[CategoryFormData] = Form(
    mapping(
      "name" -> nonEmptyText.verifying(
        Constraints.pattern(namePattern, "constraint.name", "error.name.format")
      ),
      "slug" -> nonEmptyText.verifying(
        Constraints.pattern(slugPattern, "constraint.slug", "error.slug.format")
      ),
      "color" -> shortNumber.verifying(
        "error.color.invalid", code => Category.Color.values.exists(_.code == code)
      ),
    )(CategoryFormData.apply)(CategoryFormData.unapply)
  )
}
