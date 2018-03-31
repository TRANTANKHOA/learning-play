package datacentral.dynamicPages.html

import scalatags.Text.all._
import scalatags.generic.AttrPair
import scalatags.text.Builder

object ToggleAttrs {
  val modal: AttrPair[Builder, String] = toggle("modal")
  val collapse: AttrPair[Builder, String] = toggle("collapse")
  val dropdown: AttrPair[Builder, String] = toggle("dropdown")
  val tab: AttrPair[Builder, String] = toggle("tab")
  val offcanvas: AttrPair[Builder, String] = toggle("offcanvas")
  val sidebarControl: AttrPair[Builder, String] = toggle("control-sidebar")

  def toggle(value: String): AttrPair[Builder, String] = data.toggle := value
}
