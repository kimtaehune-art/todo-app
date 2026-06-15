/**
 *
 * to do sample project
 *
 */

package model

import lib.model.Category

// Todo 新規追加ページのviewvalue (カテゴリー select 用に全カテゴリーを持つ)
case class ViewValueTodoCreate(
  title:      String,
  cssSrc:     Seq[String],
  jsSrc:      Seq[String],
  categories: Seq[Category],
) extends ViewValueCommon
