/**
 *
 * Todo REST API (SPA フロント向け。HTML ではなく JSON を返す)
 *
 */

package controllers.api

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.libs.json.{ Json, JsValue }
import play.api.data.Form
import play.api.i18n.{ I18nSupport, Messages }

import lib.persistence.{ TodoRepository, CategoryRepository }
import lib.model.{ Todo, Category }
import forms.{ TodoForm, TodoFormData }
import json.ApiWrites._

@Singleton
class TodoApiController @Inject() (
  val controllerComponents: ControllerComponents,
  todoRepository:           TodoRepository,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // GET /api/todos : 全 Todo を category 埋め込みで JSON 返却
  def list() = Action.async { implicit req =>
    val todosFuture      = todoRepository.getAll
    val categoriesFuture = categoryRepository.getAll
    for {
      todos      <- todosFuture
      categories <- categoriesFuture
    } yield {
      val categoryMap = categories.flatMap(c => c.id.map(_ -> c)).toMap
      val rows        = todos.flatMap(t => categoryMap.get(t.categoryId).map(c => TodoWithCategory(t, c)))
      Ok(Json.toJson(rows))
    }
  }

  // POST /api/todos : JSON 本文を受け取り検証して作成。status は TODO 固定
  def create() = Action.async(parse.json) { implicit req =>
    // MPA と同じ TodoForm を JSON にバインドして検証ロジックを再利用
    TodoForm.form.bind(req.body).fold(
      // 検証エラー → 400 + エラー JSON
      formWithErrors =>
        Future.successful(BadRequest(errorsJson(formWithErrors))),
      // 検証OK → 保存し、作成された Todo を category 込みで返す (201)
      data => {
        val todo = Todo(
          id         = None,
          categoryId = Category.Id(data.categoryId),
          title      = data.title,
          body       = data.body,
          state      = Todo.Status.TODO,
        )
        for {
          newId    <- todoRepository.add(todo.toWithNoId)
          savedOpt <- todoRepository.get(newId)
          catOpt   <- categoryRepository.get(Category.Id(data.categoryId))
        } yield (savedOpt, catOpt) match {
          case (Some(saved), Some(cat)) => Created(Json.toJson(TodoWithCategory(saved, cat)))
          case _                        => InternalServerError(Json.obj("message" -> "作成後の取得に失敗しました"))
        }
      }
    )
  }

  // フォームエラーを { errors: [{ key, message }] } の JSON にする
  private def errorsJson(form: Form[TodoFormData])(implicit messages: Messages): JsValue =
    Json.obj(
      "errors" -> form.errors.map(e => Json.obj("key" -> e.key, "message" -> messages(e.message, e.args: _*))),
    )
}
