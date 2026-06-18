/**
 *
 * to do sample project
 *
 */

package model

// カテゴリー編集ページのviewvalue (form の action 用に id を持つ)
case class ViewValueCategoryEdit(
  title:  String,
  cssSrc: Seq[String],
  jsSrc:  Seq[String],
  id:     Long,
) extends ViewValueCommon
