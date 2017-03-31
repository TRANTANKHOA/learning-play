package scalatags.LTETemplates

import scalatags.Text.TypedTag
import scalatags.htmllib.addStyleSheetAt

object CSSskins extends Enumeration {
  type CSSskin = Value
  val all, black, black_light, blue, blue_light = Value
  val skin: Map[CSSskin, TypedTag[String]] = Map(
    CSSskins.all -> addStyleSheetAt("dist/css/skins/_all-skins.min.css"),
    CSSskins.black -> addStyleSheetAt("dist/css/skins/skin-black.css"),
    CSSskins.black_light -> addStyleSheetAt("dist/css/skins/skin-black-light.css"),
    CSSskins.blue -> addStyleSheetAt("dist/css/skins/skin-blue.css"),
    CSSskins.blue_light -> addStyleSheetAt("dist/css/skins/skin-blue-light.css") // more to chose from in the folder.
  )
}
