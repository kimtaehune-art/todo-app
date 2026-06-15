/**
 *
 * Todo 新規追加フォーム
 *
 */

package forms

import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints

import lib.model.Todo

// 新規追加フォームの入力データ (status は含めない: 作成時は TODO 固定)
case class TodoFormData(
  title:      String,
  body:       String,
  categoryId: Long,
)

// 更新フォームの入力データ (status も編集可能なので state を持つ)
case class TodoEditFormData(
  title:      String,
  body:       String,
  categoryId: Long,
  state:      Short,
)

object TodoForm {

  // 許可文字: 英数字 + 半角スペース + 日本語 (CJK記号/ひらがな/カタカナ/漢字/全角形)
  // ※ 改行(\r,\n)は含めない
  private val allowed =
    "A-Za-z0-9 \\u3000-\\u303F\\u3040-\\u309F\\u30A0-\\u30FF\\u4E00-\\u9FFF\\uFF00-\\uFFEF"

  // Title : 改行禁止 → 改行を含まない allowed のみ
  private val titlePattern = ("[" + allowed + "]+").r
  // Body  : 改行許可 → allowed に \r\n を追加
  private val bodyPattern  = ("[" + allowed + "\\r\\n]+").r

  val form: Form[TodoFormData] = Form(
    mapping(
      "title" -> nonEmptyText.verifying(
        Constraints.pattern(titlePattern, "constraint.title", "error.title.format")
      ),
      "body" -> nonEmptyText.verifying(
        Constraints.pattern(bodyPattern, "constraint.body", "error.body.format")
      ),
      "categoryId" -> longNumber,
    )(TodoFormData.apply)(TodoFormData.unapply)
  )

  // 更新フォーム: 新規追加の制約に加え、state(0/1/2 の有効な status code) を検証
  val editForm: Form[TodoEditFormData] = Form(
    mapping(
      "title" -> nonEmptyText.verifying(
        Constraints.pattern(titlePattern, "constraint.title", "error.title.format")
      ),
      "body" -> nonEmptyText.verifying(
        Constraints.pattern(bodyPattern, "constraint.body", "error.body.format")
      ),
      "categoryId" -> longNumber,
      "state" -> shortNumber.verifying(
        "error.state.invalid", code => Todo.Status.values.exists(_.code == code)
      ),
    )(TodoEditFormData.apply)(TodoEditFormData.unapply)
  )
}
