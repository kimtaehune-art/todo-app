/**
 *
 * to do sample project
 *
 */

package controllers

import javax.inject._
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc._
import play.api.i18n.I18nSupport

import lib.persistence.CategoryRepository
import lib.model.Category
import forms.CategoryForm
import model.{ ViewValueCategoryList, ViewValueCategoryCreate }

@Singleton
class CategoryController @Inject() (
  val controllerComponents: ControllerComponents,
  categoryRepository:       CategoryRepository,
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

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

  // 新規追加フォームの表示
  def register() = Action { implicit req =>
    Ok(views.html.category.create(createViewValue, CategoryForm.form))
  }

  // 新規追加の登録処理
  def create() = Action.async { implicit req =>
    CategoryForm.form.bindFromRequest().fold(
      // 検証エラー: 入力値+エラー付きフォームを再表示 (BadRequest=400)。
      // color の選択肢は enum 由来で DB 不要なため、ここでは DB アクセスをしない。
      // ただし Action.async の戻り値は Future[Result] なので、同期の Result を Future.successful で包む。
      formWithErrors =>
        Future.successful(BadRequest(views.html.category.create(createViewValue, formWithErrors))),
      // 検証OK: 保存して一覧へ redirect (PRG)。add が Future なので自然に Future[Result] になる。
      data => {
        val category = Category(
          id    = None,
          name  = data.name,
          slug  = data.slug,
          color = Category.Color(data.color),
        )
        categoryRepository.add(category.toWithNoId).map { _ =>
          Redirect(routes.CategoryController.list())
        }
      }
    )
  }

  // create 画面用 ViewValue (color の選択肢は enum から直接引くため追加データは無し)
  private def createViewValue: ViewValueCategoryCreate =
    ViewValueCategoryCreate(
      title  = "カテゴリー新規追加",
      cssSrc = Seq("main.css", "category-form.css"),
      jsSrc  = Seq("main.js", "category-form.js"),
    )
}
