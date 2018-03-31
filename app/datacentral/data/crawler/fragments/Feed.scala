package datacentral.data.crawler.fragments

import java.util.Date

import com.rometools.rome.feed.synd.{SyndCategory, SyndFeed}

import scala.collection.JavaConverters._

case class Feed(
                 title: String,
                 uri: String,
                 description: String,
                 entries: Seq[Entry],
                 links: Seq[Link],
                 date: Date,
                 authors: Seq[Journalist],
                 contributors: Seq[Journalist],
                 editor: String,
                 webmaster: String,
                 feedType: String,
                 language: String,
                 categories: Seq[SyndCategory]
               )

object Feed {
  def apply(syndFeed: SyndFeed): Feed = new Feed(
    title = syndFeed.getTitle,
    uri = syndFeed.getUri,
    description = syndFeed.getDescription,
    entries = syndFeed.getEntries.asScala.map(Entry(_)),
    links = syndFeed.getLinks.asScala.map(Link(_)),
    editor = syndFeed.getManagingEditor,
    webmaster = syndFeed.getWebMaster,
    date = syndFeed.getPublishedDate,
    feedType = syndFeed.getFeedType,
    language = syndFeed.getLanguage,
    authors = syndFeed.getAuthors.asScala.map(Journalist(_)),
    categories = syndFeed.getCategories.asScala,
    contributors = syndFeed.getContributors.asScala.map(Journalist(_))
  )
}