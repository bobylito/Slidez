package controllers

import play.api._
import play.api.mvc._
import models.LiveStream
import scala.collection.immutable 
import play.api.data._
import play.api.data.Forms._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import lib._
import play.api.http._

import akka.util.Timeout
import akka.util.duration._

import actors.PresentationWorker
import actors.PresentationWorker._

import akka.pattern.ask

import anorm._


object Application extends Controller {

  def index = Action {
    //live stream list 
    val streams = LiveStream.findAll()
    Ok(views.html.index(streams))
  }

  def initPrez = Action { request =>
    //announced Prez
    Ok("initPrez")
  }

  val broadcastForm = Form(
    tuple (
      "id" -> number,
      "page" -> nonEmptyText
    )
  )

  def broadCast = Action { implicit request =>
    broadcastForm.bindFromRequest().fold(
      formWithErrors => BadRequest("NOT GOOD! Try again"),
      {case (id, page) => PresentationWorker.ref ! ChangePage(page)
                          Ok("broadCast")}
    ) 
  }

  def listen = Action { request =>
    //Client content fetching 
    Ok("listen")
  }

  def startPrez = Action { request =>
    //Push state to the client 
    Ok("startPrez")
  }

  def stopPrez = Action { request =>
    //Push state to the client and archive 
    Ok("stopPrez")
  }

  val eventEnum = SSEvent[String](eventName = "page-change")

  /*
 */
  def viewStream = Action {
    AsyncResult {
      implicit val timeout = Timeout(5 second)
      (PresentationWorker.ref ? Listen).mapTo[Enumerator[String]].asPromise.map({ in =>
        SimpleResult(
          header = ResponseHeader(OK, Map(
                      CONTENT_LENGTH -> "-1",
                      CONTENT_TYPE -> "text/event-stream"
                  )), 
          in &> eventEnum)
      })
    }
  }  


  def view(url : String) = Action{ request =>
    Ok(views.html.view(url))
  }

  def speakerView = Action{ request => 
    request.body.asFormUrlEncoded.get("url")
        .headOption
        .map(url => {
            LiveStream.create(LiveStream(NotAssigned, "Presentation hackday slidez", url)) 
            Ok(views.html.speaker(5, url)) 
          })
        .getOrElse(BadRequest)
  }

}
