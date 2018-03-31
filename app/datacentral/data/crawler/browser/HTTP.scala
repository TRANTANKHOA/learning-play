package datacentral.data.crawler.browser

import datacentral.data.crawler.fragments.News
import datacentral.data.utils.extensions.web.DocExtensions

import scala.language.reflectiveCalls

object HTTP {

  /**
    * Load any website using http protocol
    *
    * @param url string address plus any relevant parameters
    * @return
    */
  def load(url: String): List[News] = DocExtensions
    .maybeDoc(url)
    .map(doc => {
      val head: List[News] = doc.news.toList
      val tail: List[News] = doc.childDocs.flatMap(_.news)
      head ++ tail
    })
    .getOrElse(List.empty)
}