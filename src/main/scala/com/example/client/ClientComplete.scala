package com.example.client

import akka.actor.ActorSystem
import scala.concurrent.{Await, Future}
import spray.http._
import spray.json.DefaultJsonProtocol
import spray.httpx.SprayJsonSupport._
import spray.client.pipelining._
import scala.concurrent.duration._


object SensorProtocol extends DefaultJsonProtocol {
  case class Sensor(beach: String, waves: Seq[Int], numSurfers: Int)
  implicit val SensorFormat = jsonFormat3(Sensor)
}
import SensorProtocol._

object ClientComplete extends App {
  implicit val system = ActorSystem()
  import system.dispatcher // execution context for futures

  val apiLocation = "http://localhost:8080/private"
  val timeout = 5.seconds

  // Secure Example returning json list of sensors
  val securePipeline = addCredentials(BasicHttpCredentials("david", "1234")) ~> sendReceive
  val rez = securePipeline(Get("http://localhost:8080/private/list/all"))
  println(Await.result(rez, timeout))

  // Example returning json list of a sensor
  val pipeline = sendReceive
  val rez2 = pipeline(Get("http://localhost:8080/private/sensor?beach=Mavericks"))
  println(Await.result(rez2, timeout))


  // Example using explicit futures list of sensors
  val pipeline2: HttpRequest => Future[List[Sensor]] = sendReceive ~> unmarshal[List[Sensor]]
  val f: Future[List[Sensor]] = pipeline2(Get(s"$apiLocation/private/list/all"))
  val sensors = Await.result(f, timeout)
  println(sensors)

  Thread.sleep(1000L)
  system.shutdown()
  system.awaitTermination()
}
