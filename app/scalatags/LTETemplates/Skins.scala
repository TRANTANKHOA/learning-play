package scalatags.LTETemplates

import scalatags.Text.TypedTag
import scalatags.htmllib.addStyleSheetAt

// This is just an exercise to create a Map with enum-keys, a simple object with multiple values would suffice the purpose instead.
object Skins extends Enumeration {
  type CSSskin = Value
  val all, black, black_light, blue, blue_light = Value
  val select: Map[CSSskin, TypedTag[String]] = Map(
    Skins.all -> addStyleSheetAt("dist/css/skins/_all-skins.min.css"),
    Skins.black -> addStyleSheetAt("dist/css/skins/skin-black.css"),
    Skins.black_light -> addStyleSheetAt("dist/css/skins/skin-black-light.css"),
    Skins.blue -> addStyleSheetAt("dist/css/skins/skin-blue.css"),
    Skins.blue_light -> addStyleSheetAt("dist/css/skins/skin-blue-light.css") // more to chose from in the folder.
  )
}
