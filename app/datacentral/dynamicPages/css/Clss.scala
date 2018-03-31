package datacentral.dynamicPages.css

import datacentral.data.utils.extensions.sequences.SequenceExtensions.RichString
import datacentral.dynamicPages.LTETemplates.StaticValues

import scalatags.Text.TypedTag
import scalatags.Text.all._

object Clss {

  val holdTransition = "hold-transition"
  val wrapper = "wrapper"
  val row = "row"
  val mainFooter = "main-footer"
  val mainHeader = "main-header"
  val divider = "divider"
  val border = "with-border"
  val userPanel = "user-panel"
  val image = "image"
  val imgCircle = "img-circle"
  val inputGroup = "input-group"
  val inputGroupBtn = "input-group-btn"
  val info = "info"
  val formControl = "form-control"
  val formGroup = "form-group"
  val headerStl = "header"
  val menu = "menu"
  val footerStl = "footer"
  val hiddenXs = "hidden-xs"
  val active = "active"
  val srOnly = "sr-only"

  def colMd(sz: Int = 12) = s"col-md-$sz"

  def colXs(sz: Int = 12) = s"col-xs-$sz"

  object user {
    val header = "user-header"
    val img = "user-image"
    val body = "user-body"
    val footer = "user-footer"
  }

  object content {
    val stl = "content"
    val wrapper = "content-wrapper"
    val header = "content-header"
  }

  object box {
    val stl = "box"
    val body = "box-body"
    val header = "box-header"
    val title = "box-title"
    val tools = "box-tools"
  }

  object treeView {
    val active = "active treeview"
    val menu = "treeview-menu"
  }

  object btn {
    val stl = "btn"
    val flat = "btn-flat"
    val default = "btn-default"
    val box_tool = "btn btn-box-tool"
    val group = "btn-group"

    def flatButton(label: String = "Profile", url: String = StaticValues.topOfPage, align: String = pull.left): TypedTag[String] = div(cls := align)(
      a(href := url, cls := stl combine flat combine default)(label)
    )
  }

  object progress {
    val xs = "progress xs"
    val bar = "progress-bar progress-bar-aqua"
  }

  object skin {
    val blue = "skin-blue"
  }

  object sidebars {
    val stl = "sidebar"
    val mini = "sidebar-mini"
    val form = "sidebar-form"
    val menu = "sidebar-menu"
    val toggle = "sidebar-toggle"
    val main = "main-sidebar"
    val control = "control-sidebar"
    val homeTab = "control-sidebar-home-tab"
    val settingTab = "control-sidebar-settings-tab"
    val heading = "control-sidebar-heading"
    val subHeading = "control-sidebar-subheading"
    val control_menu = "control-sidebar-menu"
    val dark_control = "control-sidebar-dark"
    val tabs_control = "control-sidebar-tabs"
  }

  object nav {
    val justifiedTabs = "nav nav-tabs nav-justified"
    val navBar = "nav navbar-nav"
    val customMenuBar = "navbar-custom-menu"
  }

  object tab {
    val content = "tab-content"
    val pane = "tab-pane"

  }

  object pull {
    val left = "pull-left"
    val right = "pull-right"
    val rightContainer = "pull-right-container"
  }

  object fontAws {
    val fa = "fa"
    val circle = "fa-circle"
    val circle_o = "fa fa-circle-o"
    val search = "fa-search"
    val user = "fa-user"
    val users = "fa-users"
    val warning = "fa-warning"
    val shoppingCart = "fa-shopping-cart"
    val envelope = "fa fa-envelope-o"
    val bell = "fa fa-bell-o"
    val clock = "fa fa-clock-o"
    val flag = "fa fa-flag-o"
    val gears = "fa fa-gears"
    val dashboard = "fa fa-dashboard"
    val angleLeft = "fa fa-angle-left"
    val minus = "fa fa-minus"
    val times = "fa fa-times"
    val wrench = "fa fa-wrench"
    val home = "fa fa-home"

    object notifications {
      val warning: String = fa combine fontAws.warning combine text.yellow
      val users: String = fa combine fontAws.users combine text.aqua
      val user: String = fa combine fontAws.user combine text.red
      val shopping: String = fa combine shoppingCart combine text.green
    }

  }

  object text {
    val success = "text-success"
    val yellow = "text-yellow"
    val aqua = "text-aqua"
    val red = "text-red"
    val green = "text-green"
    val center = "text-center"
  }

  object status {
    val online: String = fontAws.fa combine fontAws.circle combine text.success
  }


  object labels {
    val stl = "label"
    val success = "label label-success"
    val warning = "label label-warning"
    val danger = "label label-danger"
  }

  object dropdowns {
    val stl = "dropdown"
    val toggle = "dropdown-toggle"
    val menu = "dropdown-menu"

    object menus {
      val messages: String = "dropdown messages-menu"
      val notifications: String = "dropdown notifications-menu"
      val tasks: String = "dropdown tasks-menu"
      val account: String = "dropdown user user-menu"
    }

  }

}
