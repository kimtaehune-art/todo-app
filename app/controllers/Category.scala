/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.mvc._

import lib.persistence.CategoryRepository
import model.ViewValueCategoryList

@Singleton
class CategoryController @Inject() (
  val controllerComponents: ControllerComponents,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController {

  // カテゴリー一覧表示 
  def list() = Action.async { implicit req =>
    categoryRepository.getAll.map { categories =>
      val vv = ViewValueCategoryList(
        title      = "カテゴリー一覧",
        cssSrc     = Seq("main.css", "category-list.css"),
        jsSrc      = Seq("main.js"),
        categories = categories,
      )
      Ok(views.html.category.list(vv))
    }
  }
}
