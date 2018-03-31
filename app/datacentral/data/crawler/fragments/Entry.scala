package datacentral.data.crawler.fragments

import java.util.Date

import com.rometools.rome.feed.synd.SyndEntry

import scala.collection.JavaConverters._

case class Entry(
                  title: String,
                  description: String,
                  content: Seq[String],
                  publishDate: Date,
                  authors: Seq[Journalist],
                  contributors: Seq[Journalist],
                  links: Seq[Link],
                  link2Comments: String
                )

object Entry {
  def apply(entry: SyndEntry): Entry = new Entry(
    entry.getTitle,
    entry.getDescription.getValue,
    entry.getContents.asScala.map(_.getValue),
    entry.getPublishedDate,
    entry.getAuthors.asScala.map(Journalist(_)),
    entry.getContributors.asScala.map(Journalist(_)),
    entry.getLinks.asScala.map(Link(_)),
    entry.getComments)
}