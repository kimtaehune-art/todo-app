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
import forms.{ TodoForm, TodoEditFormData }
import json.ApiWrites._

@Singleton
class TodoApiController @Inject() (
  val controllerComponents: ControllerComponents,
  todoRepository:           TodoRepository,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // GET /api/todos : 全 Todo を category 埋め込みで JSON 返却。
  // JOIN で 1 クエリにまとめて取得し、N+1 / 全件突き合わせを避ける。
  def list() = Action.async { implicit req =>
    todoRepository.getAllWithCategory.map { rows =>
      Ok(Json.toJson(rows.map { case (todo, category) => TodoWithCategory(todo, category) }))
    }
  }

  // GET /api/statuses : ステータスの選択肢 (Todo.Status を単一の出典として返す)
  def statuses() = Action {
    Ok(Json.toJson(Todo.Status.values))
  }

  // POST /api/todos : JSON 本文を受け取り検証して作成。status は TODO 固定
  def create() = Action.async(parse.json) { implicit req =>
    // MPA と同じ TodoForm を JSON にバインドして検証ロジックを再利用
    TodoForm.form.bind(req.body).fold(
      formWithErrors =>
        Future.successful(BadRequest(errorsJson(formWithErrors))),
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
          case _                        => InternalServerError(messageJson("error.fetchAfterCreate"))
        }
      }
    )
  }

  // GET /api/todos/:id : 単一 Todo を category 込みで返す。無ければ 404
  def get(id: Long) = Action.async { implicit req =>
    val todoId = Todo.Id(id)
    todoRepository.get(todoId).flatMap {
      case Some(todo) =>
        categoryRepository.get(todo.categoryId).map {
          case Some(cat) => Ok(Json.toJson(TodoWithCategory(todo, cat)))
          case None      => NotFound(messageJson("error.todo.categoryMissing"))
        }
      case None =>
        Future.successful(NotFound(messageJson("error.todo.notFound", id)))
    }
  }

  // PUT /api/todos/:id : JSON で更新 (status も編集可)。read-modify-write で createdAt 保持。無ければ 404
  def update(id: Long) = Action.async(parse.json) { implicit req =>
    // 更新は status も含む editForm を再利用して検証
    TodoForm.editForm.bind(req.body).fold(
      formWithErrors =>
        Future.successful(BadRequest(errorsJson(formWithErrors))),
      data =>
        todoRepository.get(Todo.Id(id)).flatMap {
          case Some(existing) =>
            val updated = existing.copy(
              categoryId = Category.Id(data.categoryId),
              title      = data.title,
              body       = data.body,
              state      = Todo.Status(data.state),
            )
            for {
              _        <- todoRepository.update(updated.toEmbeddedId)
              savedOpt <- todoRepository.get(Todo.Id(id))
              catOpt   <- categoryRepository.get(Category.Id(data.categoryId))
            } yield (savedOpt, catOpt) match {
              case (Some(saved), Some(cat)) => Ok(Json.toJson(TodoWithCategory(saved, cat)))
              case _                        => InternalServerError(messageJson("error.fetchAfterUpdate"))
            }
          case None =>
            Future.successful(NotFound(messageJson("error.todo.notFound", id)))
        }
    )
  }

  // DELETE /api/todos/:id : 削除。成功は 204、無ければ 404
  def delete(id: Long) = Action.async { implicit req =>
    todoRepository.remove(Todo.Id(id)).map {
      case Some(_) => NoContent
      case None    => NotFound(messageJson("error.todo.notFound", id))
    }
  }

  // フォームエラーを { errors: [{ key, message }] } の JSON にする (どのフォームでも使える)
  private def errorsJson(form: Form[_])(implicit messages: Messages): JsValue =
    Json.obj(
      "errors" -> form.errors.map(e => Json.obj("key" -> e.key, "message" -> messages(e.message, e.args: _*))),
    )

  // メッセージキーを i18n 解決して { message } の JSON にする
  private def messageJson(key: String, args: Any*)(implicit messages: Messages): JsValue =
    Json.obj("message" -> messages(key, args: _*))
}
