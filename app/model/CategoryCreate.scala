/**
 *
 * to do sample project
 *
 */

package model

// カテゴリー新規追加ページのviewvalue
// (color の選択肢は Category.Color.values から引くため、追加フィールドは持たない)
case class ViewValueCategoryCreate(
  title:  String,
  cssSrc: Seq[String],
  jsSrc:  Seq[String],
) extends ViewValueCommon
