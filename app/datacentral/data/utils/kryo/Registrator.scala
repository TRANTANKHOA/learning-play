package datacentral.data.utils.kryo

import com.esotericsoftware.kryo.Kryo
import org.apache.spark.serializer.KryoRegistrator
import org.joda.time.LocalDate

class Registrator extends KryoRegistrator {
  override def registerClasses(kryo: Kryo): Unit = kryo.register(classOf[LocalDate], new LocalDateSerializer)
}
