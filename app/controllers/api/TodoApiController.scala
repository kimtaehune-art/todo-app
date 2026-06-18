/**
 *
 * Todo REST API (SPA フロント向け。HTML ではなく JSON を返す)
 *
 */

package controllers.api

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.mvc._
import play.api.libs.json.Json

import lib.persistence.{ TodoRepository, CategoryRepository }
import json.ApiWrites._

@Singleton
class TodoApiController @Inject() (
  val controllerComponents: ControllerComponents,
  todoRepository:           TodoRepository,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController {

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
}
