package datacentral.data.testUtils

import java.io.{File, FileOutputStream}
import java.nio.charset.StandardCharsets

import datacentral.data.transform.time.{NameScore, Timer}

import scala.math.ceil

case class NoteTaker(name: String = "") {

  // Timing
  private val clock: Timer = new Timer

  private var secondaryClocks: Seq[(String, Timer)] = Seq.empty

  def addLap(marker: String) {
    clock.addLap(marker)
  }

  def getFinalLap: NameScore = clock.getFinalLap

  // Note taking
  private var note: Seq[String] = Seq("\n")
  var currentAnalysis: String = ""

  def add(newNote: String) {
    if (!note.takeRight(5).contains(newNote))
      note ++= Seq(newNote)
  }

  def addThenReturn(newNote: String): NoteTaker = {
    this.add(newNote)
    this
  }

  def addSection(section: String) {
    val n: Int = leftDivider.length - ceil(section.length / 2.0).toInt
    note ++= Seq(s"$lineDivider\n${leftDivider.take(n)}$section${rightDivider.take(n)}$lineDivider")
  }

  def getNotes: Seq[String] = note

  def copyFrom(another: NoteTaker) {
    note ++= another.getNotes
    secondaryClocks ++= Seq((another.name, another.clock))
  }

  // Formatter
  val leftDivider = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
  val rightDivider = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
  val midDivider = "ooo"
  val lineDivider = s"\n$leftDivider$midDivider$rightDivider"

  override def toString: String = {
    ((this.name, clock) +: secondaryClocks).foreach(clk => if (clk._2.nonEmpty) {
      this.add(s"${leftDivider}Timing results ${if (clk._1.nonEmpty) s"from ${clk._1}" else ""}$rightDivider\n${clk._2.toString}")
    })
    this.note.mkString("\n")
  }

  def toFile(name: String, append: Boolean = false) {
    val file = new File(name + ".log")
    if (!file.exists()) {
      file.getParentFile.mkdirs()
      file.createNewFile()
    } // if file already exists will do nothing
    new FileOutputStream(file, append).write(this.toString.getBytes(StandardCharsets.UTF_8))
  }

  def cutNoteBy(n: Int) {
    note = note.take(n)
  }

  def take(n: Int): NoteTaker = {
    val newNote = this
    newNote.cutNoteBy(n)
    newNote
  }

  def clear {
    note = Seq("\n")
    clock.reset()
  }
}
