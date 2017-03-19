package datacentral.data.transform.string

import datacentral.data.transform.sequence.SmartSequence

abstract class StringSequence(val seq: Seq[String] = Seq.empty[String]) extends SmartSequence {
  type Self <: StringSequence

  val valueSet: Set[String] = Set.empty[String] // all valid values for elements in seq

  val listNonEmptyString: Seq[String] = seq.map(_.trim).filter(_.nonEmpty)

  val length: Int = listNonEmptyString.length

  // sequence builders
  def fromSeq(seq: Seq[String]): Self

  def toSeq: Seq[String] = listNonEmptyString

  def distinct: Seq[String] = listNonEmptyString.distinct

  def map[B](f: (String) ⇒ B): Seq[B] = seq.map(f)

  def mkString(separator: String = ", "): String = listNonEmptyString.mkString(separator)

  override def toString: String = listNonEmptyString.toString + " from types: " + valueSet.toString

  def getTypeNumberAsSeq(number: Int): Self = newSeqFromString(getTypeNumber(number))

  // get elements
  def getTypeNumber(number: Int): String = if (number < valueSet.size) valueSet.toSeq(number max 0) else ""

  def getValueNumberAsSeq(number: Int): Self = newSeqFromString(getValueNumber(number))

  def newSeqFromString(string: String): Self = fromSeq(Seq(string))

  def getValueNumber(number: Int): String = if (number < listNonEmptyString.length) listNonEmptyString(number max 0) else ""

  def getFirstType: String = fromSeq(valueSet.intersect(this.toSet).toSeq).getFirstString

  def toSet: Set[String] = listNonEmptyString.distinct.toSet

  def getFirstString: String = listNonEmptyString.headOption.getOrElse("")

  // boolean conditions
  def nonEmpty: Boolean = listNonEmptyString.nonEmpty

  def isEmpty: Boolean = listNonEmptyString.isEmpty

  def containsAnyOf(testSeq: Seq[String]): Boolean = testSeq.exists(item => this.contains(item))

  def contains(item: String): Boolean = listNonEmptyString.contains(item.trim)

  // editing
  def trimBothEnds: Seq[String] = listNonEmptyString.map(
    _.stripLineEnd.trim.stripPrefix(".").stripSuffix(".").stripPrefix(",").stripSuffix(",")
  )

  def stripNonAlphaNumeric: Seq[String] = listNonEmptyString.map(
    _.replaceAll("[^a-zA-Z0-9\\s]", " ")
  )

  def addSpacesToBothEnds: Seq[String] = listNonEmptyString.map(
    " " + _ + " "
  )

  def removeMultipleSpaces: Seq[String] = listNonEmptyString.map(
    _.replaceAll("[\\s]+", " ")
  )

  def toLowerCase: Seq[String] = listNonEmptyString.map(
    _.toLowerCase
  )

  def toUpperCase: Seq[String] = listNonEmptyString.map(
    _.toUpperCase
  )
}

