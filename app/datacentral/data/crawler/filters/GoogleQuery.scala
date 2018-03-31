package datacentral.data.crawler.filters

/**
  * Created by Eric on 5/08/2017.
  */
object GoogleQuery extends WebQuery {
  override val included: Seq[String] = "khoi%20ngoai" :: Nil
  override val excluded: Seq[String] = "mua%20rong" :: Nil
  val url: String = "https://google.com/search?q=\"" +
                    s"${included.mkString("%20")}" + "\""
}
