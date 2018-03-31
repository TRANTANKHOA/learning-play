package datacentral.data.crawler.browser

import org.scalatest.{BeforeAndAfterEach, FunSuite}

class HTTPTest extends FunSuite with BeforeAndAfterEach {

  override def beforeEach() {

  }

  override def afterEach() {

  }

  test("testLoad") {
    val news = HTTP.load(url = "http://cafef.vn/buc-tranh-tuong-phan-cua-2-ong-lon-nhap-khau-than-trung-quoc-va-an-do" +
                               "-20170805143145766.chn")

    assert(news.nonEmpty)
  }

}
