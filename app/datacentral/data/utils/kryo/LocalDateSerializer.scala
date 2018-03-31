package datacentral.data.utils.kryo

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}
import org.joda.time.LocalDate

class LocalDateSerializer extends Serializer[LocalDate] with java.io.Serializable {

  override def write(kryo: Kryo, output: Output, obj: LocalDate) {
    output.writeInt(obj.getYear)
    output.writeInt(obj.getMonthOfYear)
    output.writeInt(obj.getDayOfMonth)
  }

  override def read(kryo: Kryo, input: Input, typeClass: Class[LocalDate]): LocalDate = new LocalDate(input.readInt(), input.readInt(), input.readInt())

}
