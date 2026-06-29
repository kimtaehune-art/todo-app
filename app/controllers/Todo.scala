/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.control.NonFatal
import play.api.mvc._
import play.api.i18n.I18nSupport

import lib.persistence.{ TodoRepository, CategoryRepository }
import lib.model.{ Todo, Category }
import forms.{ TodoForm, TodoEditFormData }
import model.{ ViewValueTodoList, ViewValueTodoCreate, ViewValueTodoEdit }

@Singleton
class TodoController @Inject() (
  val controllerComponents: ControllerComponents,
  todoRepository:           TodoRepository,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  // Todo一覧表示
  def list() = Action.async { implicit req =>
    // 2つのクエリを先に起動して並行実行させる (for の中で呼ぶと直列になるため)
    val todosFuture      = todoRepository.getAll
    val categoriesFuture = categoryRepository.getAll

    (for {
      todos      <- todosFuture
      categories <- categoriesFuture
    } yield {
      // category_id -> Category の対応表を作り、各 Todo に紐づくカテゴリーを引く
      val categoryMap = categories.flatMap(c => c.id.map(_ -> c)).toMap
      val rows        = todos.map(todo => (todo, categoryMap.get(todo.categoryId)))

      val vv = ViewValueTodoList(
        title  = "Todo一覧",
        cssSrc = Seq("main.css", "todo-list.css"),
        jsSrc  = Seq("main.js", "todo-list.js"),
        todos  = rows,
      )
      Ok(views.html.todo.list(vv))
    }).recover {
      // DB 取得失敗時に 500 をそのまま返さず、メッセージを表示する
      case NonFatal(_) => InternalServerError("Todo一覧の取得に失敗しました")
    }
  }

  // 新規追加フォームの表示 (空のフォーム + カテゴリー一覧)
  def register() = Action.async { implicit req =>
    categoryRepository.getAll.map { categories =>
      Ok(views.html.todo.create(viewValue(categories), TodoForm.form))
    }
  }

  // 新規追加の登録処理
  def create() = Action.async { implicit req =>
    TodoForm.form.bindFromRequest().fold(
      // 検証エラー: エラー付きフォームを再表示 (BadRequest)
      formWithErrors =>
        categoryRepository.getAll.map { categories =>
          BadRequest(views.html.todo.create(viewValue(categories), formWithErrors))
        },
      // 検証OK: status は TODO 固定で保存し、一覧へ redirect (PRG)
      data => {
        val todo = Todo(
          id         = None,
          categoryId = Category.Id(data.categoryId),
          title      = data.title,
          body       = data.body,
          state      = Todo.Status.TODO,
        )
        todoRepository.add(todo.toWithNoId).map { _ =>
          Redirect(routes.TodoController.list())
        }
      }
    )
  }

  // create 画面用 ViewValue を組み立てる小さなヘルパー
  private def viewValue(categories: Seq[Category]): ViewValueTodoCreate =
    ViewValueTodoCreate(
      title      = "Todo 新規追加",
      cssSrc     = Seq("main.css", "todo-form.css"),
      jsSrc      = Seq("main.js"),
      categories = categories,
    )

  // 更新フォームの表示 (既存データで fill。対象が無ければ 404)
  def edit(id: Long) = Action.async { implicit req =>
    val todoId           = Todo.Id(id)
    val todoFuture       = todoRepository.get(todoId)
    val categoriesFuture = categoryRepository.getAll
    for {
      todoOpt    <- todoFuture
      categories <- categoriesFuture
    } yield todoOpt match {
      case Some(todo) =>
        val filled = TodoForm.editForm.fill(
          TodoEditFormData(todo.title, todo.body, todo.categoryId, todo.state.code)
        )
        Ok(views.html.todo.edit(editViewValue(id, categories), filled))
      case None =>
        NotFound(s"Todo(id=$id) が見つかりません")
    }
  }

  // 更新処理 (read-modify-write: 既存を取得し、編集対象フィールドだけ差し替えて保存)
  def update(id: Long) = Action.async { implicit req =>
    val todoId = Todo.Id(id)
    TodoForm.editForm.bindFromRequest().fold(
      formWithErrors =>
        categoryRepository.getAll.map { categories =>
          BadRequest(views.html.todo.edit(editViewValue(id, categories), formWithErrors))
        },
      data =>
        todoRepository.get(todoId).flatMap {
          case Some(existing) =>
            val updated = existing.copy(
              categoryId = Category.Id(data.categoryId),
              title      = data.title,
              body       = data.body,
              state      = Todo.Status(data.state),
            )
            todoRepository.update(updated.toEmbeddedId).map { _ =>
              Redirect(routes.TodoController.list())
            }
          case None =>
            Future.successful(NotFound(s"Todo(id=$id) が見つかりません"))
        }
    )
  }

  // 削除処理 (remove → 一覧へ redirect。対象が無くても一覧へ戻す)
  def delete(id: Long) = Action.async { implicit req =>
    todoRepository.remove(Todo.Id(id)).map { _ =>
      Redirect(routes.TodoController.list())
    }
  }

  // edit 画面用 ViewValue を組み立てる小さなヘルパー (form の action 用に id を持つ)
  private def editViewValue(id: Long, categories: Seq[Category]): ViewValueTodoEdit =
    ViewValueTodoEdit(
      title      = "Todo 編集",
      cssSrc     = Seq("main.css", "todo-form.css"),
      jsSrc      = Seq("main.js"),
      id         = id,
      categories = categories,
    )
}
