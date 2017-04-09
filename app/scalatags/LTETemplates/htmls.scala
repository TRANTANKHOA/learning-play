package scalatags.LTETemplates

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalDateTime}

import datacentral.data.transform.sequence.Smart

import scala.util.Random
import scalatags.LTETemplates.Skins.CSSskin
import scalatags.Text.TypedTag
import scalatags.Text.all._
import scalatags.css.BootStrapToggles

object htmls extends Smart {

  val topOfPage = "#"

  val defMeta: Seq[TypedTag[String]] = Seq(
    meta(charset := "utf-8"),
    meta(httpEquiv := "X-UA-Compatible", content := "IE=edge"),
    meta(content := "width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no", name := "viewport")
  )

  val scripts: Seq[TypedTag[String]] = Scripts.getAllValsAsInstancesOf[TypedTag[String]] toSeq

  val styleSheets: Seq[TypedTag[String]] = Stylesheets.getAllValsAsInstancesOf[TypedTag[String]] toSeq

  def skin(`type`: CSSskin = Skins.blue): TypedTag[String] = Skins.select(`type`)

  def pageHead(title: String = "Data CENTRAL", color: CSSskin = Skins.blue): TypedTag[String] = head(tag("title")(title), defMeta, styleSheets, skin(color))

  def pageBody(content: Seq[TypedTag[String]] = Seq(pageHeader, mainSidebar, mainContent, mainFoot, controlSidebar), scripts: Seq[TypedTag[String]] = scripts): TypedTag[String] = body(cls := "hold-transition skin-blue sidebar-mini")(div(cls := "wrapper")(content), scripts)

  def mainSidebar: TypedTag[String] = {}

  def mainContent: TypedTag[String] = {}

  def mainFoot: TypedTag[String] = {}

  def controlSidebar: TypedTag[String] = {}

  def pageHeader: TypedTag[String] = header(cls := "main-header")(logo(), headerNavigator())

  def logo: TypedTag[String] = simpleClickTo(clss = "logo", display = Seq(
    span(cls := "logo-mini")(b("D"), "CTL"),
    span(cls := "logo-lg")(b("Data"), "CENTRAL")))

  def simpleClickTo(url: String = "javascript:alert('This clickable object should be linked to somewhere. Just check out the document for a tags and href to link this to the main page. This popup is just a place holder for now.');",
                    clss: String = "noclass",
                    display: Frag): TypedTag[String] = a(href := url, cls := clss)(display)

  def headerNavigator(sidebar: Seq[TypedTag[String]] = Seq(dropdown(), dropdownNotifications(), dropdownTasks, account, controlSidebarToggle)): TypedTag[String] = tag("nav")(cls := "navbar navbar-static-top")(
    a(href := topOfPage, cls := "sidebar-toggle", BootStrapToggles.offcanvas, role := "button")(
      span(cls := "sr-only")("Toggle navigation")
    ),
    div(cls := "navbar-custom-menu")(
      ul(cls := "nav navbar-nav")(sidebar)
    )
  )

  val imgFolder = "dist/img/"

  def message(msg: String = "Do we need ScalaJS, or we are fine with ScalaTags only?", time: LocalDateTime = LocalDateTime.now().plusSeconds(-5), sender: String = "Data Engineer", icon: String = "user2-160x160.jpg"): TypedTag[String] = {
    val timeString: String = time2String

    def time2String = {
      val now = LocalDateTime.now()
      require(time.isBefore(now), s"You've got a message from the future $time!!!")
      val duration = Duration.between(time, now)
      val days = duration.toDays
      val hours = duration.minusDays(days).toHours
      val minutes = duration.minusHours(hours + days * 24).toMinutes
      val seconds = duration.minusMinutes(minutes + (hours + days * 24) * 60).getSeconds

      def numToLabel(input: (Long, String)): String = input._1 match {
        case 0 => ""
        case 1 => s"1 ${input._2}"
        case n => s"$n ${input._2}s"
      }

      (days, "day") :: (hours, "hour") :: (minutes, "minute") :: (seconds, "second") :: Nil map numToLabel mkString " " removeMultipleSpaces
    }

    li(
      a(href := topOfPage)(
        div(cls := "pull-left")(
          img(src := imgFolder + icon, cls := "img-circle", alt := "User Icon")
        ),
        h4(
          sender,
          small(i(cls := FontAwesomeIcons.clock), s"$timeString ago")
        ),
        p(msg)
      )
    )
  }

  def notification(url: String = topOfPage, notificationType: String = FontAwesomeNotifications.users, msg: String = "5 new members joined today"): TypedTag[String] = li(a(href := url)(i(cls := notificationType), s" ${msg.trim}"))

  def dropdown(items: Seq[TypedTag[String]], itemType: String, icon: String, label: String, dropdownType: String): TypedTag[String] =
    li(cls := dropdownType)(
      a(href := topOfPage, cls := BootStrapDropdown.toggle, BootStrapToggles.dropdown)(
        i(cls := icon),
        span(cls := label)(items.length)
      ),
      ul(cls := BootStrapDropdown.menu)(
        li(BootStrapSimple.header)(stringFrag(s"You have ${items.length} ${itemType}s")),
        li(ul(BootStrapSimple.menu)(items)),
        li(BootStrapSimple.footer)(simpleClickTo(display = s"See all ${itemType}s"))
      )
    )

  def dropdownMessages(messages: Seq[TypedTag[String]] = Seq(message())): TypedTag[String] = dropdown(
    items = messages, itemType = "message", icon = FontAwesomeIcons.envelope, label = BootstrapLabel.success, dropdownType = DropdownMenus.messages
  )

  def dropdownNotifications(notifications: Seq[TypedTag[String]] = Seq(notification())): TypedTag[String] = dropdown(
    items = notifications, itemType = "notification", icon = FontAwesomeIcons.bell, label = BootstrapLabel.warning, dropdownType = DropdownMenus.notifications
  )

  def dropdownTasks(tasks: Seq[TypedTag[String]] = Seq(task())): TypedTag[String] = dropdown(
    items = tasks, itemType = "task", icon = FontAwesomeIcons.flag, label = BootstrapLabel.danger, dropdownType = DropdownMenus.tasks
  )

  def account(url: String = topOfPage, icon: String = "user2-160x160.jpg", name: String = "Alexander Pierce", jobTitle: String = "Data Engineer", startDate: LocalDate = LocalDate.now.minusDays(Random.nextInt(1000))): TypedTag[String] = li(cls := DropdownMenus.account)(
    a(href := url, cls := BootStrapDropdown.toggle, BootStrapToggles.dropdown)(
      img(src := imgFolder + icon, cls := "user-image", alt := "User Image"),
      span(cls := "hidden-xs")(name)
    ),
    ul(cls := BootStrapDropdown.menu)(
      li(cls := "user-header")(
        img(src := imgFolder + icon, cls := "img-circle", alt := "User Image"),
        p(
          s"$name - $jobTitle",
          small(s"Member since ${startDate.format(DateTimeFormatter.ofPattern("MMM. yyyy"))}")
        ) // keep working here
      )
    )
  )

  def task(url: String = topOfPage, msg: String = "Deploy the sparkjob-server", completion: Int = Random.nextInt(100)) = li(
    a(href := url)(
      h3(
        msg, small(cls := AlignText.right)(s"$completion%")
      ),
      div(cls := "progress xs")(
        div(cls := "progress-bar progress-bar-aqua", style := s"width: $completion%", role := "progressbar", aria.valuenow := completion.toString, aria.valuemin := "0", aria.valuemax := "100")(
          span(cls := "sr-only")(s"$completion% Complete")
        )
      )
    )
  )

  object AlignText {
    val right = "pull-right"
    val left = "pull-left"
  }

  object DropdownMenus {
    val messages: String = "dropdown messages-menu"
    val notifications: String = "dropdown notifications-menu"
    val tasks: String = "dropdown tasks-menu"
    val account: String = "dropdown user user-menu"
  }

  object FontAwesomeIcons {
    val envelope = "fa fa-envelope-o"
    val bell = "fa fa-bell-o"
    val clock = "fa fa-clock-o"
    val flag = "fa fa-flag-o"
  }

  object FontAwesomeNotifications {
    val warning = "fa fa-warning text-yellow"
    val users = "fa fa-users text-aqua"
    val user = "fa fa-user text-red"
    val shopping = "fa fa-shopping-cart text-green"
  }

  object BootstrapLabel {
    val success = "label label-success"
    val warning = "label label-warning"
    val danger = "label label-danger"
  }

  object BootStrapDropdown {
    val toggle = "dropdown-toggle"
    val menu = "dropdown-menu"
  }

  object BootStrapSimple {
    val header = cls := "header"
    val menu = cls := "menu"
    val footer = cls := "footer"
  }

  def controlSidebarToggle: TypedTag[String] = li(a(href := topOfPage, BootStrapToggles.sidebarControl)(i(cls := "fa fa-gears")))

}



