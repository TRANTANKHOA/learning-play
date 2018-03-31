package datacentral.data.crawler.filters

import com.neovisionaries.i18n.{CountryCode, CurrencyCode, LanguageCode}

abstract class WebQuery {

  val included: Seq[String]

  val excluded: Seq[String]
}


case class Company(
                    name: String,
                    country: Seq[Country],
                    person: Seq[Person],
                    product: Seq[Product],
                    event: Seq[Event]
                  ) extends WebQuery {
  override val included: Seq[String] = Seq(name) ++
                                       person.map(_.name) ++
                                       product.map(_.name) ++
                                       event.map(_.name) ++
                                       country.map(_.name.getName)

  override val excluded: Seq[String] = ???
}


case class Person(name: String, company: String, country: Country) extends WebQuery {
  override val included: Seq[String] = ???
  override val excluded: Seq[String] = ???
}


case class Product(name: String, company: String, country: Country) extends WebQuery {
  override val included: Seq[String] = ???
  override val excluded: Seq[String] = ???
}


case class Event(name: String, company: String, country: Country) extends WebQuery {
  override val included: Seq[String] = ???
  override val excluded: Seq[String] = ???
}


case class Bank(name: String, country: CountryCode, currency: CurrencyCode) extends WebQuery {
  override val included: Seq[String] = ???
  override val excluded: Seq[String] = ???
}

case class Country(name: CountryCode, bank: Seq[Bank], currency: CurrencyCode, language: LanguageCode) extends WebQuery {
  override val included: Seq[String] = ???
  override val excluded: Seq[String] = ???
}
