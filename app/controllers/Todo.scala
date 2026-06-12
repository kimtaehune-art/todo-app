/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._

import lib.persistence.{ TodoRepository, CategoryRepository }
import model.ViewValueTodoList

@Singleton
class TodoController @Inject() (
  val controllerComponents: ControllerComponents,
  todoRepository:           TodoRepository,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController {

  // Todo一覧表示
  def list() = Action.async { implicit req =>
    // 2つのクエリを先に起動して並行実行させる (for の中で呼ぶと直列になるため)
    val todosFuture      = todoRepository.getAll
    val categoriesFuture = categoryRepository.getAll

    for {
      todos      <- todosFuture
      categories <- categoriesFuture
    } yield {
      // category_id -> Category の対応表を作り、各 Todo に紐づくカテゴリーを引く
      val categoryMap = categories.flatMap(c => c.id.map(_ -> c)).toMap
      val rows        = todos.map(todo => (todo, categoryMap.get(todo.categoryId)))

      val vv = ViewValueTodoList(
        title  = "Todo一覧",
        cssSrc = Seq("main.css", "todo-list.css"),
        jsSrc  = Seq("main.js"),
        todos  = rows,
      )
      Ok(views.html.todo.list(vv))
    }
  }
}
