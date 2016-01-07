package com.example.server

import com.example.{NewSensor, Sensor, Buoy, NewBuoy}
import akka.actor.Actor
import akka.actor.{Props, Actor, ActorSystem}
import spray.http.HttpHeaders.Authorization
import spray.http.{BasicHttpCredentials, MediaTypes}
import spray.routing._
import spray.http._
import spray.routing.authentication.{UserPass, BasicUserContext, UserPassAuthenticator, BasicAuth}
import scala.concurrent.{Promise, Future}
import scala.io.Source

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiActor extends Actor with Api {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(this.route)
}

trait Api extends ApiPrivateService with ApiPublicService {
  lazy val route = {
    pathPrefix("private") {apiPrivateRoute} ~ pathPrefix("public") {apiPublicRoute}
  }
}

// this trait defines our service behavior independently from the service actor
trait ApiPrivateService extends HttpService {
  var plentyOfSensors = Sensor.sensors

  def getJson(route: Route) = get {
    respondWithMediaType(MediaTypes.`application/json`) {route}
  }

  val userName = headerValuePF {
    case Authorization(BasicHttpCredentials(user, _)) =>  user
  }

  lazy val apiPrivateRoute =
    path("up") {
        get {
          userName { user =>
            complete {
              s"Sensors up $user!"
            }
          }
        }
    } ~
    path("list" / "all") {
      getJson {
        complete {
          Sensor.toJson(plentyOfSensors)
        }
      }
    } ~
    path("sensor") {
      getJson(
        parameters("beach"?) { location =>
          val beachspec = plentyOfSensors.find(x => x.beach == location.getOrElse("Mavericks")).get
          complete {
            Sensor.toJson(beachspec)
          }
        }
      )
    } ~
    path("add" / "sensor") {
      post {
        parameters("beach", "wave".as[Int], "numSurfers".as[Int]) { (beach, wave, numSurfers) =>
          val newsensor = NewSensor(beach, Seq(wave), numSurfers)
          plentyOfSensors = newsensor :: plentyOfSensors
          complete {
            "Sensor added!"
          }
        }
      }
    } ~
    path("sensor" / "lastwaves") {
      get {
        parameters("beach", "wavecnt".as[Int]) { (beach, wavecnt) =>
          val sensor = plentyOfSensors.find(x => x.beach == beach).get
          complete {
            sensor.waves.take(wavecnt).toString()
          }
        }
      }
    }
}



trait ApiPublicService extends HttpService {
  var plentyOfBuoy = Buoy.sensors

  def getJson2(route: Route) = get {
    respondWithMediaType(MediaTypes.`application/json`) {route}
  }

  lazy val apiPublicRoute =
    path("buoy") {
      getJson2 {
        complete {
          Buoy.toJson(plentyOfBuoy)
        }
      }
    }
}

