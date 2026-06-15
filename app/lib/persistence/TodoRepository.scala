/**
  * This is a sample of Todo Application.
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
)(implicit val ec:         ExecutionContext) extends SlickRepository[Todo.Id, Todo] {

  val todoTable = TableQuery[TodoTable]

  /**
    * Get all Todo records (一覧表示用)
    */
  def getAll: Future[Seq[Todo]] = {
    slave.run(todoTable.result)
  }

  /**
    * Add a new Todo (新規追加). 採番された id を返す。書き込みは master。
    */
  def add(todo: Todo#WithNoId): Future[Todo.Id] = {
    master.run(todoTable returning todoTable.map(_.id) += todo.v)
  }

  /**
    * Get a single Todo by id (更新フォームの初期表示用). 読み取りは slave。
    */
  def get(id: Todo.Id): Future[Option[Todo]] = {
    slave.run(todoTable.filter(_.id === id).result.headOption)
  }

  /**
    * Update an existing Todo (更新). 更新できれば Some、対象が無ければ None。書き込みは master。
    */
  def update(entity: Todo#EmbeddedId): Future[Option[Todo#EmbeddedId]] = {
    master.run {
      todoTable.filter(_.id === entity.id).update(entity.v).map(_ > 0).map {
        case true  => Some(entity)
        case false => None
      }
    }
  }

  /**
    * Delete a Todo by id (削除). 削除できれば Some(id)、対象が無ければ None。書き込みは master。
    */
  def remove(id: Todo.Id): Future[Option[Todo.Id]] = {
    master.run {
      todoTable.filter(_.id === id).delete.map {
        case 0 => None
        case _ => Some(id)
      }
    }
  }
}
