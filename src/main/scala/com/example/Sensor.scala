package com.example

import org.json4s.native.Serialization
import org.json4s.native.Serialization._
import org.json4s.ShortTypeHints
import scala.util.Random

trait Sensor {
  def beach : String
  def waves : Seq[Int]
}

case class NewSensor(beach: String, waves: Seq[Int], numSurfers: Int) extends Sensor

object Sensor {
  val sensors = List[Sensor](
    NewSensor("Mavericks", Seq.fill(10)(Random.nextInt(100)), 5),
    NewSensor("SteamersLane", Seq.fill(10)(Random.nextInt(100)), 10),
    NewSensor("Trestles", Seq.fill(10)(Random.nextInt(100)), 7)
  )

  private implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[NewSensor])))
  def toJson(sensors: List[Sensor]): String = writePretty(sensors)
  def toJson(sensor: Sensor): String = writePretty(sensor)
}