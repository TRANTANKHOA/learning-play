package datacentral.data.crawler.fragments

import com.rometools.rome.feed.synd.SyndPerson

case class Journalist(name: String, email: String)

object Journalist {
  def apply(person: SyndPerson): Journalist = new Journalist(person.getName, person.getEmail)
}