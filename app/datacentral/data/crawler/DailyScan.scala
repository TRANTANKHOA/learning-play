package datacentral.data.crawler

import datacentral.data.crawler.browser.HTTP
import datacentral.data.crawler.fragments.News
import datacentral.data.utils.concurent.Task
import datacentral.data.utils.concurent.Task.FutureTried

import scala.concurrent.ExecutionContext.Implicits.global

object DailyScan extends App {

  val websites = Seq(
    //    "http://cafef.vn/",
    //    "http://vietnambiz.vn/",
    //    "http://vneconomy.vn/",
    "http://vietstock.vn/",
    "https://www.stockbiz.vn/",
    "https://thitruongcaosu.net/",
    "http://tapchicaosu.vn/",
    "http://ndh.vn/"
  ) //.par

  val task = Task(websites.map(HTTP.load).seq.flatten)

  val httpNews: Seq[News] = task.get

  print(s"Duration = ${task.duration.get / 1000L} secs")

  //  val googleAlerts = Seq(
  //    KeyValue("cao su", "https://www.google.com/alerts/feeds/15776121918240313822/9279690690172513035"),
  //    KeyValue("cao su đà nẵng", "https://www.google.com/alerts/feeds/15776121918240313822/13747175234655144083"),
  //    KeyValue("DRC", "https://www.google.com/alerts/feeds/15776121918240313822/2519319411922323205"),
  //    KeyValue("lốp xe", "https://www.google.com/alerts/feeds/15776121918240313822/9279690690172514097"),
  //    KeyValue("săm lốp", "https://www.google.com/alerts/feeds/15776121918240313822/5426801732447335232")
  //  )
  //
  //  val rssNews = googleAlerts.map(each => RSS.load(each.value)).filter(_.isDefined)

}