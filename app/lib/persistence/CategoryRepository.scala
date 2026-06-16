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
}
