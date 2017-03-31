package scalatags.LTETemplates

import common.Reflections
import scalatags.Text.TypedTag
import scalatags.htmllib.addStyleSheetAt

object Stylesheets extends Reflections {
  val bootstrap: TypedTag[String] = addStyleSheetAt("bootstrap/css/bootstrap.min.css")
  val fontAwesome: TypedTag[String] = addStyleSheetAt("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css")
  val ionIcons: TypedTag[String] = addStyleSheetAt("https://cdnjs.cloudflare.com/ajax/libs/ionicons/2.0.1/css/ionicons.min.css")
  val theme: TypedTag[String] = addStyleSheetAt("dist/css/AdminLTE.min.css")
  val iCheck: TypedTag[String] = addStyleSheetAt("plugins/iCheck/flat/blue.css")
  val morrisChart: TypedTag[String] = addStyleSheetAt("plugins/morris/morris.css")
  val jvectormap: TypedTag[String] = addStyleSheetAt("plugins/jvectormap/jquery-jvectormap-1.2.2.css")
  val datePicker: TypedTag[String] = addStyleSheetAt("plugins/datepicker/datepicker3.css")
  val dateRangePicker: TypedTag[String] = addStyleSheetAt("plugins/daterangepicker/daterangepicker.css")
  val wysihtml5TextEditor: TypedTag[String] = addStyleSheetAt("plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css")
}



