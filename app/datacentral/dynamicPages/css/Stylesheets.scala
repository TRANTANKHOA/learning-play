package datacentral.dynamicPages.css

import datacentral.dynamicPages.html.Play.stylesheet

import scalatags.Text.TypedTag

object Stylesheets {
  val bootstrap: TypedTag[String] = stylesheet("bootstrap/css/bootstrap.min.css")
  val fontAwesome: TypedTag[String] = stylesheet("https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.5.0/css/font-awesome.min.css")
  val ionIcons: TypedTag[String] = stylesheet("https://cdnjs.cloudflare.com/ajax/libs/ionicons/2.0.1/css/ionicons.min.css")
  val theme: TypedTag[String] = stylesheet("dist/css/AdminLTE.min.css")
  val iCheck: TypedTag[String] = stylesheet("plugins/iCheck/flat/blue.css")
  val morrisChart: TypedTag[String] = stylesheet("plugins/morris/morris.css")
  val jvectormap: TypedTag[String] = stylesheet("plugins/jvectormap/jquery-jvectormap-1.2.2.css")
  val datePicker: TypedTag[String] = stylesheet("plugins/datepicker/datepicker3.css")
  val dateRangePicker: TypedTag[String] = stylesheet("plugins/daterangepicker/daterangepicker.css")
  val wysihtml5TextEditor: TypedTag[String] = stylesheet("plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.min.css")
}



