package datacentral.data.utils.extensions.sequences

/**
  * These extensions are mainly concerned with improving readability for code involving collections, e.g. avoiding `*._1`
  * etc.. due to using `Map`
  *
  * One reason for this is because it helps when reading test files.
  */
object TupleExtensions {

  implicit class KeyValue[A, B](tuple2: (A, B)) {
    def key: A = tuple2._1

    def value: B = tuple2._2
  }

  case class ZipKey[Key, A](key: Key, value: A)

  case class ZipIndex[A](index: Int, value: A)

  implicit class TraversableMapWrapper[A, B](seq: Traversable[(A, B)]) {
    def distinctByKey: Seq[(A, B)] = seq.distinctBy(_.key)
  }

  implicit class TraversableOnceWrapper[A](seq: TraversableOnce[A]) {

    def distinctBy[Key](key: A => Key): Seq[A] = seq.zipIndex
      .groupBy(each => key(each.value))
      .values.map(_.head).toSeq
      .sortBy(_.index).map(_.value)

    def zipIndex: Seq[ZipIndex[A]] = seq.toSeq.zipWithIndex.map(a => ZipIndex(a._2, a._1))

    def getOneNextTo(predicate: A => Boolean): Option[A] = zipIndex
      .find(item => predicate(item.value))
      .map(i => seq.toSeq(i.index))

    def zipWithKey[Key](key: A => Key): TraversableOnce[ZipKey[Key, A]] = seq.map(a => ZipKey(key(a), a))

    def zipAsKey[Key](keys: Seq[Key]): Seq[ZipKey[Key, A]] = {
      require(keys.length == seq.size, s"There are ${keys.length} keys to match to ${seq.size} elements")
      seq.toSeq.zip(keys).map(pair => ZipKey(pair._2, pair._1))
    }

    def none(predicate: A => Boolean): Boolean = !seq.exists(predicate)

    def map2String: Seq[String] = seq.map(_.toString).toSeq

    def sameAs(
                another: Traversable[A],
                equality: (A, A) => Boolean = (a, b) => a == b
              )(implicit ord: Ordering[A]): Boolean = {
      val leftS = seq.toSeq.sorted(ord)
      val rightS = another.toSeq.sorted(ord)

      leftS.length == rightS.length && leftS.zip(rightS).forall(p => equality(p._1, p._2))
    }
  }

}
