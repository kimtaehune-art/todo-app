/**
  * This is a sample of Todo Application.
  *
  */

package lib.persistence

import scala.concurrent.{ ExecutionContext, Future }
import ixias.slick.SlickRepository
import ixias.slick.jdbc.MySQLProfile.api._
import lib.model.Category
import lib.persistence.db.{ CategoryTable, TodoTable }

import javax.inject._

// CategoryRepository: CategoryTableへのクエリ発行を行うRepository層の定義
//~~~~~~~~~~~~~~~~~~~~~~
@Singleton
class CategoryRepository @Inject() (
  @Named("master") master: Database, // 書き込み用 (add など)。一覧の getAll は slave のみ使用
  @Named("slave") slave:   Database
)(implicit val ec: ExecutionContext) extends SlickRepository[Category.Id, Category] {

  val categoryTable = TableQuery[CategoryTable]
  val todoTable     = TableQuery[TodoTable]

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

  /**
    * Get a single Category by id (更新フォームの初期表示用). 読み取りは slave。
    */
  def get(id: Category.Id): Future[Option[Category]] = {
    slave.run(categoryTable.filter(_.id === id).result.headOption)
  }

  /**
    * Update an existing Category (更新). 更新できれば Some、対象が無ければ None。書き込みは master。
    */
  def update(entity: Category#EmbeddedId): Future[Option[Category#EmbeddedId]] = {
    master.run {
      categoryTable.filter(_.id === entity.id).update(entity.v).map(_ > 0).map {
        case true  => Some(entity)
        case false => None
      }
    }
  }

  /**
    * Delete a Category and its associated Todos (削除).
    * 関連 Todo の削除とカテゴリーの削除を 1 トランザクションで実行する。
    * 削除できれば Some(id)、対象が無ければ None。書き込みは master。
    */
  def remove(id: Category.Id): Future[Option[Category.Id]] = {
    val action = (for {
      _ <- todoTable.filter(_.categoryId === id).delete     // 関連 Todo を先に削除
      n <- categoryTable.filter(_.id === id).delete          // カテゴリー本体を削除
    } yield n).transactionally                               // 2つをまとめて1トランザクションに
    master.run(action).map {
      case 0 => None
      case _ => Some(id)
    }
  }
}
