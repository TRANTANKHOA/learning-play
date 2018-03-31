package datacentral.data.utils.logging

import datacentral.data.utils.functional.SmartFunctions.tryTwiceOrPrintStackTrace
import org.apache.http.HttpHost
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{BasicResponseHandler, HttpClients}
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.{AppenderSkeleton, Priority}

case class HttpAppender(
                         proxyHostname: Option[String] = None,
                         proxyPort: Int = 80,
                         proxyScheme: String = "http",
                         url: Option[String] = None
                       ) extends AppenderSkeleton {

  override def append(event: LoggingEvent) {

    event.setProperty(
      "color",
      if (event.getLevel.toInt >= Priority.ERROR_INT) {
        "danger"
      } else if (event.getLevel.toInt >= Priority.WARN_INT) {
        "warning"
      } else {
        "good"
      }
    )

    // Make sure the json doesn't contain newlines.
    val message = this.layout.format(event)
      .replace("\r\n", "\\n")
      .replace("\n", "\\n")
      .replace("\r", "\\n")

    val post = new HttpPost(url match {
      case Some(str) => str
      case None => throw new RuntimeException(s"Please set url for HttpAppender = ${this.toString}")
    })
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity(message, "UTF-8"))

    val client = HttpClients.createDefault()

    if (proxyHostname.isDefined) {
      post.setConfig(
        RequestConfig.custom()
          .setProxy(new HttpHost(proxyHostname.get, proxyPort, proxyScheme))
          .build()
      )
    }

    val responseHandler = new BasicResponseHandler
    tryTwiceOrPrintStackTrace(client.execute(post, responseHandler))
    tryTwiceOrPrintStackTrace(client.close())
  }

  override def requiresLayout(): Boolean = true

  override def close(): Unit = {}
}