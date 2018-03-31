package datacentral.data.crawler.browser

import datacentral.data.crawler.fragments.News
import org.scalatest.{BeforeAndAfterEach, FunSuite}

class RSSTest extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testLoad") {
    val news: Option[Seq[News]] = RSS.load(link = "http://cafef.vn/trang-chu.rss")

    assert(news.get.nonEmpty)
  }

}
