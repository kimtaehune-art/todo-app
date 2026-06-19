/**
 *
 * Category REST API (SPA フロント向け / JSON)
 *
 */

package controllers.api

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.libs.json.{ Json, JsValue }
import play.api.data.Form
import play.api.i18n.{ I18nSupport, Messages }

import lib.persistence.CategoryRepository
import lib.model.Category
import forms.CategoryForm
import json.ApiWrites._

@Singleton
class CategoryApiController @Inject() (
  val controllerComponents: ControllerComponents,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // GET /api/categories : 全カテゴリーを JSON 返却 (Todo 追加フォームの選択肢などに使う)
  def list() = Action.async { implicit req =>
    categoryRepository.getAll.map { categories =>
      Ok(Json.toJson(categories))
    }
  }

  // GET /api/colors : カラーの選択肢 (Category.Color を単一の出典として返す)
  def colors() = Action {
    Ok(Json.toJson(Category.Color.values))
  }

  // POST /api/categories : JSON 本文を検証して作成。201 で作成された Category を返す
  def create() = Action.async(parse.json) { implicit req =>
    // MPA と同じ CategoryForm を JSON にバインドして検証 (slug は英数字のみ等)
    CategoryForm.form.bind(req.body).fold(
      formWithErrors =>
        Future.successful(BadRequest(errorsJson(formWithErrors))),
      data => {
        val category = Category(
          id    = None,
          name  = data.name,
          slug  = data.slug,
          color = Category.Color(data.color),
        )
        for {
          newId    <- categoryRepository.add(category.toWithNoId)
          savedOpt <- categoryRepository.get(newId)
        } yield savedOpt match {
          case Some(saved) => Created(Json.toJson(saved))
          case None        => InternalServerError(Json.obj("message" -> "作成後の取得に失敗しました"))
        }
      }
    )
  }

  // フォームエラーを { errors: [{ key, message }] } の JSON にする
  private def errorsJson(form: Form[_])(implicit messages: Messages): JsValue =
    Json.obj(
      "errors" -> form.errors.map(e => Json.obj("key" -> e.key, "message" -> messages(e.message, e.args: _*))),
    )
}
