package datacentral.data.crawler.filters

import java.net.URL

sealed trait WebItem {

  val title: String

  val url: URL

  def parse(address: String): this.type

}

object WebItem {
}

case class WebConversation(title: String, url: URL) extends WebItem {
  override def parse(address: String) = ???
}

case class WebArticle(title: String, url: URL) extends WebItem {
  override def parse(address: String) = ???
}

case class WebProduct(title: String, url: URL) extends WebItem {
  override def parse(address: String) = ???
}

case class WebVideo(title: String, url: URL) extends WebItem {
  override def parse(address: String) = ???
}

case class WebImage(title: String, url: URL) extends WebItem {
  override def parse(address: String) = ???
}