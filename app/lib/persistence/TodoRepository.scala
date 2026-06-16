/**
  * This is a sample of Todo Application.
  *
  */

package lib.persistence

import scala.concurrent.{ ExecutionContext, Future }
import ixias.slick.SlickRepository
import ixias.slick.jdbc.MySQLProfile.api._
import lib.model.Todo
import lib.persistence.db.TodoTable

import javax.inject._

// TodoRepository: TodoTableへのクエリ発行を行うRepository層の定義
//~~~~~~~~~~~~~~~~~~~~~~
@Singleton
class TodoRepository @Inject() (
  @Named("master") master: Database, // 書き込み用 (add/update/remove)。一覧の getAll は slave のみ使用
  @Named("slave") slave:   Database
)(implicit val ec: ExecutionContext) extends SlickRepository[Todo.Id, Todo] {

  val todoTable = TableQuery[TodoTable]

  /**
    * Get all Todo records (一覧表示用)
    */
  def getAll: Future[Seq[Todo]] = {
    slave.run(todoTable.result)
  }
}
