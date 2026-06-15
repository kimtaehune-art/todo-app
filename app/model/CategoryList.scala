/**
 *
 * to do sample project
 *
 */

package model

import lib.model.Category

// カテゴリー一覧ページのviewvalue
case class ViewValueCategoryList(
  title:      String,
  cssSrc:     Seq[String],
  jsSrc:      Seq[String],
  categories: Seq[Category],
) extends ViewValueCommon
