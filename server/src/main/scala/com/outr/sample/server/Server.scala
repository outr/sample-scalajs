package com.outr.sample.server

import java.io.File

import akka.actor.ActorSystem
import com.outr.sample.Services
import com.outr.scribe.Logging
import spray.routing.SimpleRoutingApp
import upickle._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Server extends App with SimpleRoutingApp with Logging {
  implicit val system = ActorSystem()

  val content = new File("../content")

  startServer("localhost", 8000) {
    get {
      pathSingleSlash {
        getFromFile(new File(content, "index.html"))
      } ~
      getFromDirectory(content.getAbsolutePath)
    } ~
    post {
      path("service" / Segments) { segments =>
        extract(_.request.entity.asString) { entity =>
          complete {
            dispatch(segments, entity)
          }
        }
      }
    }
  }

  def dispatch(path: List[String], args: String): Future[String] = {
    upickle.json.read(args) match {
      case Js.Obj(args @ _*) => {
        val router = ServerServices.route[Services](ServicesImplementation)
        router(autowire.Core.Request(path, args.toMap)).map(upickle.json.write(_, 0))
      }
      case _ => Future.failed(new Exception("Arguments need to be a valid JSON object"))
    }
  }
}
