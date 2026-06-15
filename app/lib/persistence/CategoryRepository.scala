/**
  * This is a sample of Todo Application.
  *
  */

package lib.persistence

import scala.concurrent.{ ExecutionContext, Future }
import ixias.slick.SlickRepository
import ixias.slick.jdbc.MySQLProfile.api._
import lib.model.Category
import lib.persistence.db.CategoryTable

import javax.inject._

// CategoryRepository: CategoryTableへのクエリ発行を行うRepository層の定義
//~~~~~~~~~~~~~~~~~~~~~~
@Singleton
class CategoryRepository @Inject() (
  @Named("master") master: Database, // 書き込み用 (add など)。一覧の getAll は slave のみ使用
  @Named("slave") slave:   Database
)(implicit val ec: ExecutionContext) extends SlickRepository[Category.Id, Category] {

  val categoryTable = TableQuery[CategoryTable]

  /**
    * Get all Category records (一覧表示用)
    */
  def getAll: Future[Seq[Category]] = {
    slave.run(categoryTable.result)
  }

  /**
    * Add a new Category (新規追加). 採番された id を返す。書き込みは master。
    */
  def add(category: Category#WithNoId): Future[Category.Id] = {
    master.run(categoryTable returning categoryTable.map(_.id) += category.v)
  }
}
