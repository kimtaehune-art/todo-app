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

  // GET /api/categories/:id : 単一カテゴリーを返す。無ければ 404
  def get(id: Long) = Action.async { implicit req =>
    categoryRepository.get(Category.Id(id)).map {
      case Some(category) => Ok(Json.toJson(category))
      case None           => NotFound(Json.obj("message" -> s"Category(id=$id) が見つかりません"))
    }
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

  // PUT /api/categories/:id : JSON で更新。read-modify-write で createdAt 保持。無ければ 404
  def update(id: Long) = Action.async(parse.json) { implicit req =>
    CategoryForm.form.bind(req.body).fold(
      formWithErrors =>
        Future.successful(BadRequest(errorsJson(formWithErrors))),
      data =>
        categoryRepository.get(Category.Id(id)).flatMap {
          case Some(existing) =>
            val updated = existing.copy(
              name  = data.name,
              slug  = data.slug,
              color = Category.Color(data.color),
            )
            for {
              _        <- categoryRepository.update(updated.toEmbeddedId)
              savedOpt <- categoryRepository.get(Category.Id(id))
            } yield savedOpt match {
              case Some(saved) => Ok(Json.toJson(saved))
              case None        => InternalServerError(Json.obj("message" -> "更新後の取得に失敗しました"))
            }
          case None =>
            Future.successful(NotFound(Json.obj("message" -> s"Category(id=$id) が見つかりません")))
        }
    )
  }

  // DELETE /api/categories/:id : 削除。関連 Todo も一緒に削除される (Repository 側でトランザクション)。
  // 成功は 204、無ければ 404
  def delete(id: Long) = Action.async { implicit req =>
    categoryRepository.remove(Category.Id(id)).map {
      case Some(_) => NoContent
      case None    => NotFound(Json.obj("message" -> s"Category(id=$id) が見つかりません"))
    }
  }

  // フォームエラーを { errors: [{ key, message }] } の JSON にする
  private def errorsJson(form: Form[_])(implicit messages: Messages): JsValue =
    Json.obj(
      "errors" -> form.errors.map(e => Json.obj("key" -> e.key, "message" -> messages(e.message, e.args: _*))),
    )
}
