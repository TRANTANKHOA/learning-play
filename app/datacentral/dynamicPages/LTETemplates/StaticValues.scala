package datacentral.dynamicPages.LTETemplates

import datacentral.data.utils.extensions.sequences.SequenceExtensions.RichString
import datacentral.data.utils.reflection.Functions.ReflectiveObject
import datacentral.dynamicPages.css.Clss.{btn, fontAws}
import datacentral.dynamicPages.css.{Clss, Stylesheets}
import datacentral.dynamicPages.js.Scripts

import scala.language.postfixOps
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.generic.AttrPair
import scalatags.text.Builder

object StaticValues {
  val productName = "Data CENTRAL"
  val imgFolder = "dist/img/"
  val userImg = "user2-160x160.jpg"
  val userName = "Alexander Pierce"
  val userJob = "Data Engineer"
  val topOfPage = "#"
  val defaultMeta: Seq[TypedTag[String]] = Seq(
    meta(charset := "utf-8"),
    meta(httpEquiv := "X-UA-Compatible", content := "IE=edge"),
    meta(content := "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no", name := "viewport")
  )
  val allScripts: Seq[TypedTag[String]] = Scripts.getAllValsAsInstancesOf[TypedTag[String]].get.toSeq.reverse

  val allStyleSheets: Seq[TypedTag[String]] = Stylesheets.getAllValsAsInstancesOf[TypedTag[String]].get toSeq

  val aside: TypedTag[String] = tag("aside")
  val section: TypedTag[String] = tag("section")
  val titleTag: TypedTag[String] = tag("title")
  val navTag: TypedTag[String] = tag("nav")
  val methodGet: AttrPair[Builder, String] = method := "get"
  val methodPost: AttrPair[Builder, String] = method := "post"

  def textInput(attrPairs: AttrPair[Builder, String]*): TypedTag[String] = input(tpe := "text", cls := Clss.formControl, attrPairs)

  def submitButton(attrPairs: AttrPair[Builder, String]*): TypedTag[String] = button(tpe := "submit", cls := Clss.btn.stl combine Clss.btn.flat, attrPairs)

  def collapseButton: TypedTag[String] = button(tpe := "button", cls := btn.box_tool, data.widget := "collapse")(i(cls := fontAws.minus))

  def settingButton(attrPairs: AttrPair[Builder, String]*): TypedTag[String] = button(tpe := "button", attrPairs)(i(cls := fontAws.wrench))

  def removeButton: TypedTag[String] = button(tpe := "button", cls := btn.box_tool, data.widget := "remove")(i(cls := fontAws.times))

  def listDivider: TypedTag[String] = li(cls := Clss.divider)

  def simpleClickTo(url: String = "javascript:alert('This clickable object should be linked to somewhere. Just check out the document for a tags and href to link this to the main page. This popup is just a place holder for now.');",
                    clss: String = "noclass",
                    display: Frag): TypedTag[String] = a(href := url, cls := clss)(display)
}
