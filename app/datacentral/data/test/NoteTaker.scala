package datacentral.data.test

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import datacentral.data.transform.time.Timer

case class NoteTaker(name: String = "") {

  // Formatter
  val leftDivider = "<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<"
  val rightDivider = ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
  val midDivider = "ooo"
  val lineDivider = s"\n$leftDivider$midDivider$rightDivider"
  // Timing
  private val clock: Timer = new Timer
  private var secondaryClocks: Seq[(String, Timer)] = Seq.empty
  // Note taking
  private var note: Seq[String] = Seq("\n")

  def addLap(marker: String) {
    clock.addLap(marker)
  }

  def addSection(section: String) {
    note ++= Seq(s"$lineDivider\n$leftDivider$section$rightDivider$lineDivider")
  }

  def copyFrom(another: NoteTaker) {
    note ++= another.getNotes
    secondaryClocks ++= Seq((another.name, another.clock))
  }

  def getNotes: Seq[String] = note

  def toFile(name: String) {
    Files.write(Paths.get(name + ".log"), this.toString.getBytes(StandardCharsets.UTF_8))
  }

  override def toString: String = {
    ((this.name, clock) +: secondaryClocks).foreach(clk => if (clk._2.nonEmpty) {
      this.add(s"${leftDivider}Timing results ${if (clk._1.nonEmpty) s"from ${clk._1}" else ""}$rightDivider\n${clk._2.toString}")
    })
    this.note.mkString("\n")
  }

  def add(newNote: String) {
    if (!note.takeRight(5).contains(newNote))
      note ++= Seq(newNote)
  }

  def take(n: Int): NoteTaker = {
    val newNote = this
    newNote.cutNoteBy(n)
    newNote
  }

  def cutNoteBy(n: Int) {
    note = note.take(n)
  }

  def clear {
    note = Seq("\n")
    clock.reset()
  }
}
