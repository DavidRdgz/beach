package com.example.server

import com.example.{NewSensor, Sensor}
import akka.actor.Actor
import akka.actor.{Props, Actor, ActorSystem}
import spray.http.HttpHeaders.Authorization
import spray.http.{BasicHttpCredentials, MediaTypes}
import spray.routing._
import spray.http._
import spray.routing.authentication.{UserPass, BasicUserContext, UserPassAuthenticator, BasicAuth}
import scala.concurrent.{Promise, Future}

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

trait Api extends ApiService with UserAccountService {
  lazy val route = {
    apiRoute ~ userRoute
  }
}

// this trait defines our service behavior independently from the service actor
trait ApiService extends HttpService {
  var plentyOfSensors = Sensor.sensors

  def getJson(route: Route) = get {
    respondWithMediaType(MediaTypes.`application/json`) {route}
  }

  val userName = headerValuePF {
    case Authorization(BasicHttpCredentials(user, _)) =>  user
  }

  lazy val apiRoute =
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

trait UserAccountService extends HttpService {
  lazy val userRoute =
    path("userProfile") {
      get {
        complete {
          "User up!"
        }
      }
    }
}

