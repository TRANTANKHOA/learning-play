package common

import play.api.http.{ContentTypeOf, ContentTypes, Writeable}
import play.api.mvc.Codec

import scalatags.Text.all._

trait ControllerWriters {
  val htmlDocString = "<!DOCTYPE html>\n"

  implicit def contentTypeOfTag(implicit codec: Codec): ContentTypeOf[Tag] = {
    ContentTypeOf[Tag](Some(ContentTypes.HTML))
  }

  implicit def writeableOfTag(implicit codec: Codec): Writeable[Tag] = {
    Writeable(tag => codec.encode(htmlDocString + tag.render))
  }

}
