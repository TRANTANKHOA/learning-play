package datacentral.dynamicPages.LTETemplates

import java.time.format.DateTimeFormatter
import java.time.{Duration, LocalDate, LocalDateTime}

import datacentral.data.utils.extensions.sequences.SequenceExtensions.RichString
import datacentral.dynamicPages.LTETemplates.StaticValues._
import datacentral.dynamicPages.css.Clss.{box, btn, dropdowns, fontAws, labels, nav, pull, sidebars, skin, status, tab, treeView, _}
import datacentral.dynamicPages.css.Skins.CSSskin
import datacentral.dynamicPages.css.{Clss, Skins}
import datacentral.dynamicPages.html.ToggleAttrs
import datacentral.dynamicPages.js.Scripts

import scala.language.postfixOps
import scala.util.Random
import scalatags.Text.TypedTag
import scalatags.Text.all._

object IndexPage {

  def getPage: TypedTag[String] = html(lang := "en")(
    pageHead(),
    pageBody()
  )

  def pageHead(name: String = productName, color: CSSskin = Skins.all): TypedTag[String] = head(titleTag(name), defaultMeta, allStyleSheets, Skins.select(color), Scripts.html5shiv, Scripts.respond)

  def pageBody(content: Seq[TypedTag[String]] = pageHeader :: mainSidebar() :: mainContent :: mainFoot() :: controlSidebar :: Nil, scripts: Seq[TypedTag[String]] = allScripts): TypedTag[String] = body(cls := holdTransition combine skin.blue combine sidebars.mini)(div(cls := wrapper)(content), scripts)

  def mainSidebar(avartar: String = imgFolder / userImg, usrname: String = userName, usrUrl: String = topOfPage, usrStatus: (String, String) = (status.online, "Online")): TypedTag[String] = aside(cls := sidebars.main)(
    section(cls := sidebars.stl)(
      div(cls := userPanel)(
        div(cls := pull.left combine image)(
          img(src := avartar, cls := imgCircle, alt := "User Image")
        ),
        div(cls := pull.left combine info)(
          p(usrname),
          a(href := usrUrl)(i(cls := usrStatus._1), usrStatus._2)
        )
      ),
      form(action := topOfPage, methodGet, cls := sidebars.form)(
        div(cls := inputGroup)(
          textInput(name := "q", placeholder := "Search ...")(
            span(cls := inputGroupBtn)(
              submitButton(name := "search", id := "search-btn")(i(cls := fontAws.fa combine fontAws.search))
            )
          )
        )
      ),
      ul(cls := sidebars.menu)(
        sidebarSection()
      )
    )
  )

  def sidebarSection(sectionLabel: String = "Dashboard", menus: Seq[TypedTag[String]] = Seq(dropdownMenu())): Seq[TypedTag[String]] = li(cls := headerStl)(sectionLabel) +: menus

  def dropdownMenu(label: String = "Data Warehouse", items: Seq[TypedTag[String]] = Seq.fill(2)(menuItem())): TypedTag[String] = li(cls := treeView.active)(
    a(href := topOfPage)(
      i(cls := fontAws.dashboard), span(label), span(cls := pull.rightContainer)(i(cls := fontAws.angleLeft combine pull.right))
    ),
    ul(cls := treeView.menu)(items)
  )

  def menuItem(activ: Boolean = false, url: String = "index.html", text: String = "Dashboard v1"): TypedTag[String] = li(cls := (if (activ) active else ""))(
    a(href := url)(i(cls := fontAws.circle_o), s" $text")
  )

  //noinspection RedundantDefaultArgument
  def mainContent: TypedTag[String] = div(cls := Clss.content.wrapper)(
    section(cls := Clss.content.header)(h1("Show ", small("dynamically composed query texts"))),
    section(cls := Clss.content.stl, id := "drawingboard")(
      div(cls := row)(
        div(cls := Clss.colMd(12))(
          div(cls := box.stl)(
            div(cls := box.header combine Clss.border)(
              h3(cls := box.title)("Board title"),
              div(cls := box.tools combine pull.right)(
                collapseButton,
                div(cls := btn.group)(
                  settingButton(cls := btn.box_tool combine dropdowns.toggle, ToggleAttrs.dropdown),
                  ul(cls := dropdowns.menu, role := menu)(
                    li(a(href := topOfPage)("Action")),
                    li(a(href := topOfPage)("Link to an action")),
                    li(a(href := topOfPage)("etc...")),
                    listDivider,
                    li(a(href := topOfPage)("Seperated action"))
                  )
                ),
                removeButton
              )
            ),
            div(cls := box.body)(
              div(cls := row)(
                span("Some data illustration here ")
              )
            )
          )
        )
      )
    )
  )

  def mainFoot(version: String = "0.1"): TypedTag[String] = footer(cls := mainFooter)(
    div(cls := pull.right combine hiddenXs)(b("Version"), s" $version"),
    strong("Theme's copyright &copy; 2014-2016 ", a(href := "http://almsaeedstudio.com")("Almsaeed Studio"), "."), " All rights reserved"
  )

  def controlSidebar: TypedTag[String] = aside(cls := sidebars.control combine sidebars.dark_control)(
    ul(cls := nav.justifiedTabs combine sidebars.tabs_control)(
      li(a(href := s"#${sidebars.homeTab}", ToggleAttrs.tab)(i(cls := fontAws.home))),
      li(a(href := s"#${sidebars.settingTab}", ToggleAttrs.tab)(i(cls := fontAws.gears)))
    ),
    div(cls := tab.content)(
      div(cls := tab.pane, id := sidebars.homeTab)(
        h3(cls := sidebars.heading)("Heading label"),
        ul(cls := sidebars.control_menu)(
          li("First item"),
          li("Second item")
        )
      ),
      div(cls := tab.pane, id := sidebars.settingTab)(
        form(methodPost)(
          h3(cls := sidebars.heading)("General Settings"),
          div(cls := formGroup)(
            label(cls := sidebars.subHeading)(
              "First setting", input(tpe := "checkbox", cls := pull.right, checked)
            ),
            p("Some information about first setting")
          ),
          div(cls := formGroup)(
            label(cls := sidebars.subHeading)(
              "Second setting", input(tpe := "checkbox", cls := pull.right, checked)
            ),
            p("Some information about second setting")
          )
        )
      )
    )
  )

  def pageHeader: TypedTag[String] = header(cls := mainHeader)(logo(), headerNavigator())

  def logo: TypedTag[String] = simpleClickTo(clss = "logo", display = Seq(
    span(cls := "logo-mini")(b("D"), "CTL"),
    span(cls := "logo-lg")(b("Data"), "CENTRAL"))
  )

  def headerNavigator(sidebar: Seq[TypedTag[String]] = Seq(dropdownMessages(), dropdownNotifications(), dropdownTasks(), accountProfile(), controlSidebarToggle)): TypedTag[String] = navTag(cls := "navbar navbar-static-top")(
    a(href := topOfPage, cls := sidebars.toggle, ToggleAttrs.offcanvas, role := "button")(
      span(cls := srOnly)("Toggle navigation")
    ),
    div(cls := nav.customMenuBar)(
      ul(cls := nav.navBar)(sidebar)
    )
  )

  def message(msg: String = "Do we need ScalaJS, or we are fine with ScalaTags only?", time: LocalDateTime = LocalDateTime.now().plusSeconds(-5), sender: String = "Data Engineer", icon: String = "user2-160x160.jpg"): TypedTag[String] = {
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
          small(i(cls := fontAws.clock), s"$time2String ago")
        ),
        p(msg)
      )
    )
  }

  def notification(url: String = topOfPage, notificationType: String = fontAws.notifications.users, msg: String = "5 new members joined today"): TypedTag[String] = li(a(href := url)(i(cls := notificationType), s" ${msg.trim}"))

  def dropdownMessages(messages: Seq[TypedTag[String]] = Seq(message())): TypedTag[String] = dropdown(
    items = messages, itemType = "message", icon = fontAws.envelope, label = labels.success, dropdownType = dropdowns.menus.messages
  )

  def dropdown(items: Seq[TypedTag[String]], itemType: String, icon: String, label: String, dropdownType: String): TypedTag[String] =
    li(cls := dropdownType)(
      a(href := topOfPage, cls := dropdowns.toggle, ToggleAttrs.dropdown)(
        i(cls := icon),
        span(cls := label)(items.length)
      ),
      ul(cls := dropdowns.menu)(
        li(cls := headerStl)(stringFrag(s"You have ${items.length} ${itemType}s")),
        li(ul(cls := menu)(items)),
        li(cls := footerStl)(simpleClickTo(display = s"See all ${itemType}s"))
      )
    )

  def dropdownNotifications(notifications: Seq[TypedTag[String]] = Seq(notification())): TypedTag[String] = dropdown(
    items = notifications, itemType = "notification", icon = fontAws.bell, label = labels.warning, dropdownType = dropdowns.menus.notifications
  )

  def dropdownTasks(tasks: Seq[TypedTag[String]] = Seq(task())): TypedTag[String] = dropdown(
    items = tasks, itemType = "task", icon = fontAws.flag, label = labels.danger, dropdownType = dropdowns.menus.tasks
  )

  def accountProfile(url: String = topOfPage, icon: String = userImg, name: String = userName, jobTitle: String = userJob, startDate: LocalDate = LocalDate.now.minusDays(Random.nextInt(1000))): TypedTag[String] = li(cls := dropdowns.menus.account)(
    a(href := url, cls := dropdowns.toggle, ToggleAttrs.dropdown)(
      img(src := imgFolder + icon, cls := user.img, alt := "User Image"),
      span(cls := hiddenXs)(name)
    ),
    ul(cls := dropdowns.menu)(
      li(cls := user.header)(
        img(src := imgFolder + icon, cls := imgCircle, alt := "User Image"),
        p(
          s"$name - $jobTitle",
          small(s"Member since ${startDate.format(DateTimeFormatter.ofPattern("MMM. yyyy"))}")
        )
      ),
      li(cls := user.body)(div(cls := row)(userlink(), userlink(label = "Sales"), userlink(label = "Friends"))),
      li(cls := user.footer)(btn.flatButton(), btn.flatButton(label = "Sign out", align = pull.right))
    )
  )

  def userlink(url: String = topOfPage, label: String = "Followers"): TypedTag[String] = div(cls := colXs(4) combine text.center)(a(href := url)(label))

  def task(url: String = topOfPage, msg: String = "Deploy the sparkjob-server", completion: Int = Random.nextInt(100)): TypedTag[String] = li(
    a(href := url)(
      h3(
        msg, small(cls := pull.right)(s"$completion%")
      ),
      div(cls := progress.xs)(
        div(cls := progress.bar, style := s"width: $completion%", role := "progressbar", aria.valuenow := completion.toString, aria.valuemin := "0", aria.valuemax := "100")(
          span(cls := srOnly)(s"$completion% Complete")
        )
      )
    )
  )

  def controlSidebarToggle: TypedTag[String] = li(a(href := topOfPage, ToggleAttrs.sidebarControl)(i(cls := fontAws.gears)))

}



