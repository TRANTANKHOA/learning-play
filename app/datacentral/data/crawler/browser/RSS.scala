package datacentral.data.crawler.browser

import java.net.URL

import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import datacentral.data.crawler.fragments.News
import datacentral.data.utils.functional.SmartFunctions

import scala.collection.JavaConverters._

object RSS {

  def load(link: String): Option[Seq[News]] = {
    val x = SmartFunctions
      .getSomeOrNone(
        new SyndFeedInput().build(
          new XmlReader(
            new URL(link))))

    x.map(feed => {
      val y = feed.getEntries.asScala.par
      y.map(entry => {
        val link = entry.getLink
        HTTP.load(link)
      }).flatten.seq.distinct
    })
  }
}