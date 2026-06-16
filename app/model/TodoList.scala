/**
 *
 * to do sample project
 *
 */

package model

import lib.model.{ Todo, Category }

// Todo一覧ページのviewvalue
// 各 Todo に、それが属する Category (見つからなければ None) を組にして渡す
case class ViewValueTodoList(
  title:  String,
  cssSrc: Seq[String],
  jsSrc:  Seq[String],
  todos:  Seq[(Todo, Option[Category])],
) extends ViewValueCommon
