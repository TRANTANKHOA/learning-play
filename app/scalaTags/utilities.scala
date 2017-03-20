package scalaTags

import scalatags.Text.TypedTag
import scalatags.Text.all._

object utilities {
  val index: String = "<!DOCTYPE html>" + html(
    head(
      script(src := "..."),
      script(
        "alert('Hello World')"
      )
    ),
    body(
      div(
        h1(id := "title", "This is my title"),
        p("This is a big paragraph of text")
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
