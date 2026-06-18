/**
 *
 * Category REST API (SPA フロント向け / JSON)
 *
 */

package controllers.api

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.mvc._
import play.api.libs.json.Json

import lib.persistence.CategoryRepository
import json.ApiWrites._

@Singleton
class CategoryApiController @Inject() (
  val controllerComponents: ControllerComponents,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController {

  // GET /api/categories : 全カテゴリーを JSON 返却 (Todo 追加フォームの選択肢などに使う)
  def list() = Action.async { implicit req =>
    categoryRepository.getAll.map { categories =>
      Ok(Json.toJson(categories))
    }
  }
}
