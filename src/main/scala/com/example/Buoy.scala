package com.example

import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints
import scala.io.Source
import scala.util.Random

trait Buoy {
  def beach : String
  def wvht : Array[Double]
  def dpd  : Array[Int]
  def apd  : Array[Double]
  def mwd  : Array[String]
  def wtmp  : Array[Double]

}

case class NewBuoy(beach: String, wvht: Array[Double], dpd: Array[Int], apd: Array[Double], mwd: Array[String] ,wtmp: Array[Double]) extends Buoy

object Buoy {
  val html = Source.fromURL("http://www.ndbc.noaa.gov/data/realtime2/46237.txt")
  val df = html.getLines.toList // gets rows
  val h1 = df(0)
  val h2 = df(1)
  val buoy = df.drop(2).filterNot(_.isEmpty).map { line =>
    line.split(" ").filter(e => e != "")
  }.toArray.transpose

  val sensors = List[Buoy](
    NewBuoy("SanFrancisco", buoy(8).map(_.toDouble), buoy(9).map(_.toInt), buoy(10).map(_.toDouble), buoy(11).map(_.toString), buoy(14).map(_.toDouble))
  )

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[NewBuoy])))
  def toJson(sensors: List[Buoy]): String = writePretty(sensors)
  def toJson(sensor: Buoy): String = writePretty(sensor)
  def toJson(sensor: Array[Double]): String = writePretty(sensor)
  def toJson(sensor: Array[Int]): String = writePretty(sensor)
  def toJson(sensor: Array[String]): String = writePretty(sensor)
}