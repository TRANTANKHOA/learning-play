package datacentral.dynamicPages.js

import scalatags.Text.TypedTag
import scalatags.Text.all._

object Scripts {
  val html5shiv: TypedTag[String] = script(src := "https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js")
  val respond: TypedTag[String] = script(src := "https://oss.maxcdn.com/respond/1.4.2/respond.min.js")
  val jQuery: TypedTag[String] = script(src := "plugins/jQuery/jquery-2.2.3.min.js")
  val jQueryUI: TypedTag[String] = script(src := "https://code.jquery.com/ui/1.11.4/jquery-ui.min.js")
  //Resolve conflict in jQuery UI tooltip with Bootstrap tooltip
  val conflict: TypedTag[String] = script("$.widget.bridge('uibutton', $.ui.button);")
  val bootstrap: TypedTag[String] = script(src := "bootstrap/js/bootstrap.min.js")
  val morrisCharts: TypedTag[String] = script(src := "https://cdnjs.cloudflare.com/ajax/libs/raphael/2.1.0/raphael-min.js")
  val morrisCharts2: TypedTag[String] = script(src := "plugins/morris/morris.min.js")
  val sparkline: TypedTag[String] = script(src := "plugins/sparkline/jquery.sparkline.min.js")
  val jvectormap: TypedTag[String] = script(src := "plugins/jvectormap/jquery-jvectormap-1.2.2.min.js")
  val jvectormapWorldMill: TypedTag[String] = script(src := "plugins/jvectormap/jquery-jvectormap-world-mill-en.js")
  val jQueryKnob: TypedTag[String] = script(src := "plugins/knob/jquery.knob.js")
  val moment: TypedTag[String] = script(src := "https://cdnjs.cloudflare.com/ajax/libs/moment.js/2.11.2/moment.min.js")
  val daterangepicker: TypedTag[String] = script(src := "plugins/daterangepicker/daterangepicker.js")
  val datepicker: TypedTag[String] = script(src := "plugins/datepicker/bootstrap-datepicker.js")
  val WYSIHTML5: TypedTag[String] = script(src := "plugins/bootstrap-wysihtml5/bootstrap3-wysihtml5.all.min.js")
  val Slimscroll: TypedTag[String] = script(src := "plugins/slimScroll/jquery.slimscroll.min.js")
  val FastClick: TypedTag[String] = script(src := "plugins/fastclick/fastclick.js")
  val AdminLTE: TypedTag[String] = script(src := "dist/js/app.min.js")
  //  val dashboardDemo: TypedTag[String] = script(src := "dist/js/pages/dashboard.js") // TODO need to translate these to ScalaJS
  //  val AdminLTEdemo: TypedTag[String] = script(src := "dist/js/demo.js")
}