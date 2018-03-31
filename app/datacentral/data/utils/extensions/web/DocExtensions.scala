package datacentral.data.utils.extensions.web

import datacentral.data.crawler.fragments.News
import datacentral.data.utils.extensions.sequences.SequenceExtensions.StringSequence
import datacentral.data.utils.functional.SmartFunctions.getSomeOrNone
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element, TextNode}
import org.jsoup.select.Elements

import scala.collection.JavaConverters._

object DocExtensions {

  private val notLink: Seq[String] = Seq("comment", "void", "script")

  implicit class ElementWrapper(element: Element) {
    lazy val asText: Option[String] = getSomeOrNone(element.childNode(0).asInstanceOf[TextNode].text)
    lazy val headerRank: Option[Int] = element.tag().getName match {
      case str: String if str.matches("h[1-9]") => Some(str.replaceAll("[^1-9]", "").toInt)
      case _ => None
    }
  }

  def maybeDoc(url: String): Option[DocWrapper] = getSomeOrNone(new DocWrapper(Jsoup.connect(url).get))

  implicit class DocWrapper(val doc: Document) {

    lazy val links: List[String] = doc
      .getElementsByAttribute("href").iterator().asScala.toList
      .map(_.attr("href")).distinct
      .flatMap(url => Option(if (url.contains("http")) url else if (url.nonEmpty) doc.baseUri() + url.tail else null))
      .filter(_.contains(doc.baseUri()))
      .filterNot(url => notLink.exists(word => url.contains(word)) || url == doc.baseUri())
      .sortBy(_.length).reverse

    lazy val childDocs: List[DocWrapper] = links.flatMap(maybeDoc)
    lazy val media: Elements = doc.select("[src]")
    lazy val imports: Elements = doc.select("link[href]")
    lazy val titleElements: Seq[Element] = doc.select(".title").asScala
    lazy val titles: Seq[String] = titleElements.flatMap(_.asText)
    lazy val description: Option[String] = for {
      rank <- titleElements.headOption.flatMap(_.headerRank)
      description <- doc.select(s"h${rank + 1}").first.asText
    } yield description
    lazy val bodyStrings: Option[Seq[String]] = getSomeOrNone(
      doc.body()
        .select("p").asScala
        .groupBy(_.parent.id)
        .values.toSeq
        .map(_.map(_.asText))
        .maxBy(_.aggregate(0)(
          seqop = (int, maybeString) => int + (maybeString match {
            case Some(str) => str.length
            case None => 0
          }),
          combop = _ + _))
        .map(_.getOrElse(""))
        .listNonEmptyString)
    lazy val news: Option[News] = getSomeOrNone(News(titles.head, description.getOrElse(""), bodyStrings.get))
  }

}

object BrowserEmulator extends App {

  import com.gargoylesoftware.htmlunit.WebClient
  import com.gargoylesoftware.htmlunit.html.HtmlPage

  val webClient: WebClient = new WebClient
  val page: HtmlPage = webClient.getPage("http://vietstock.vn")
  val pageAsXml: String = page.asXml
  val pageAsText: String = page.asText

}
