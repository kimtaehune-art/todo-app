/**
 *
 * REST API 用の JSON 変換 (Writes)。フロントの TS モデル契約に対応する。
 *
 */

package json

import play.api.libs.json._
import lib.model.{ Todo, Category }

object ApiWrites {

  // Color → { code, name, rgb }
  implicit val colorWrites: Writes[Category.Color] = (c: Category.Color) =>
    Json.obj("code" -> c.code.toInt, "name" -> c.name, "rgb" -> c.rgb)

  // Status → { code, name }
  implicit val statusWrites: Writes[Todo.Status] = (s: Todo.Status) =>
    Json.obj("code" -> s.code.toInt, "name" -> s.name)

  // Category → { id, name, slug, color }
  implicit val categoryWrites: Writes[Category] = (c: Category) =>
    Json.obj(
      "id"    -> c.id.map(id => (id: Long)),
      "name"  -> c.name,
      "slug"  -> c.slug,
      "color" -> c.color,
    )

  // 一覧用: Todo に category を埋め込んで返すための組
  case class TodoWithCategory(todo: Todo, category: Category)

  // TodoWithCategory → { id, title, body, status, category, createdAt, updatedAt }
  implicit val todoWithCategoryWrites: Writes[TodoWithCategory] = (x: TodoWithCategory) =>
    Json.obj(
      "id"        -> x.todo.id.map(id => (id: Long)),
      "title"     -> x.todo.title,
      "body"      -> x.todo.body,
      "status"    -> x.todo.state,
      "category"  -> x.category,
      "createdAt" -> x.todo.createdAt.toString,
      "updatedAt" -> x.todo.updatedAt.toString,
    )
}
