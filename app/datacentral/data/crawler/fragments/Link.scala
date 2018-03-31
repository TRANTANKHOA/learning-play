package datacentral.data.crawler.fragments

import com.rometools.rome.feed.synd.SyndLink

case class Link(href: String, description: String, title: String)

object Link {
  def apply(link: SyndLink): Link = new Link(link.getHref, link.getType, link.getTitle)
}