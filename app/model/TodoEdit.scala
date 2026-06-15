/**
 *
 * to do sample project
 *
 */

package model

import lib.model.Category

// Todo 編集ページのviewvalue (form の action 用に id、カテゴリー select 用に categories を持つ)
case class ViewValueTodoEdit(
  title:      String,
  cssSrc:     Seq[String],
  jsSrc:      Seq[String],
  id:         Long,
  categories: Seq[Category],
) extends ViewValueCommon
