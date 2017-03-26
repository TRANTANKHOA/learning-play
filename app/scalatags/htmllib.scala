package scalatags

import controllers.routes

import scalatags.Text.TypedTag
import scalatags.Text.all._

object htmllib {

  val playVersion: String = play.core.PlayVersion.current
  //  val playUrl =  play.core.PlayVersion.

  def index(message: String): TypedTag[String] = html(lang := "en")(
    head(
      tag("title")("Welcome to DC"),
      link(rel := "stylesheet", media := "screen", href := s"${routes.Assets.versioned("stylesheets/main.css")}"),
      link(rel := "shortcut icon", `type` := "image/png", href := s"${routes.Assets.versioned("images/favicon.png")}"),
      script(src := s"${routes.Assets.versioned("javascripts/hello.js")}", `type` := "text/javascript")
    ),
    body(link(rel := "stylesheet", media := "screen", href := "/@documentation/resources/style/main.css"))(
      tag("section")(id := "top")(
        div(`class` := "wrapper")(
          h1(a(href := s"https://playframework.com/documentation/$playVersion/Home")(message))
        )
      ),
      div(id := "content", `class` := "wrapper doc")(
        tag("article")(
          h1("How we can help"),
          p("Introduce you to business use case"),
          h2("Examples analysis results"),
          p("Play documentation is available at ",
            a(href := "https://www.playframework.com/documentation/2.5.13")("https://www.playframework.com/documentation"),
            "."),
          p("Play comes with lots of example templates showcasing various bits of Play functionality at ",
            a(href := "https://www.playframework.com/download#examples")("https://www.playframework.com/download#examples"),
            "."),
          h2("Sign in or Create account")
        ),
        tag("aside")(
          h3("Data Warehouse"),
          ul(
            li(a(href := "https://playframework.com/documentation/2.5.13")("Documentation")),
            li(a(href := "https://playframework.com/documentation/2.5.13/api/scala/index.html")("Browse the Scala API"))
          ),
          h3("Tutorials"),
          ul(
            li(a(href := "https://playframework.com/documentation/2.5.13/PlayConsole")("Using the Play console")),
            li(a(href := "https://playframework.com/documentation/2.5.13/IDE")("Setting up your preferred IDE")),
            li(a(href := "https://playframework.com/download#examples")("Example Projects"))
          ),
          h3("Forums"),
          ul(
            li(a(href := "https://stackoverflow.com/questions/tagged/playframework")("Stack Overflow")),
            li(a(href := "http://groups.google.com/group/play-framework")("Mailing List")),
            li(a(href := "https://gitter.im/playframework/playframework")("Gitter Channel"))
          )
        )
      )
    )
  )

  def imgBox(source: String, text: String): TypedTag[String] = div(
    img(src := source),
    div(
      p(text)
    )
  )

  def page(title: String, scripts: Seq[Modifier], content: Seq[Modifier]): TypedTag[String] =
    html(
      head(scripts),
      body(
        h1(title),
        div(cls := "content")(content)
      )
    )
}
