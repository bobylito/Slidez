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
import anorm._
import models.LogSlide

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
    tuple(
      "id" -> number,
      "page" -> nonEmptyText,
      "tick" -> number))

  def broadCast = Action { implicit request =>
    broadcastForm.bindFromRequest().fold(
      formWithErrors => BadRequest("NOT GOOD! Try again"),
      {
        case (id, page, tick) =>
          LogSlide.insert(LogSlide(NotAssigned, id, page, tick))
          PresentationWorker.ref ! ChangePage(id, page)
          Ok("broadCast")
      })
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
  def viewStream(id: Long) = Action {
    LiveStream.findById(id) match {
      case Some(l: LiveStream) => {
        AsyncResult {
          implicit val timeout = Timeout(5 second)
          (PresentationWorker.ref ? Listen(id)).mapTo[Enumerator[String]].asPromise.map({ in =>
            SimpleResult(
              header = ResponseHeader(OK, Map(
                CONTENT_LENGTH -> "-1",
                CACHE_CONTROL -> "no-cache",
                CONTENT_TYPE -> "text/event-stream")),
              in &> eventEnum)
          })
        }
      }
      case None => {
        BadRequest("")
      }
    }
  }
  
  def listeners = Action{
    Ok
  }

  def view(id: Long) = Action { request =>
    LiveStream.findById(id) match {
      case Some(l: LiveStream) => {
        Ok(views.html.view(l.url, l.id.get))
      }
      case None => {
        BadRequest
      }

    }

  }

  def speakerView = Action { request =>
    request.body.asFormUrlEncoded.get("url")
      .headOption
      .map(url => {
        LiveStream.create(LiveStream(NotAssigned, "Presentation hackday slidez", url)) match {
          case Some(id) => {
            PresentationWorker.ref ! NewPresentation(id)
            Ok(views.html.speaker(id, url))
          }
          case None => {
            BadRequest
          }
        }
      })
      .getOrElse(BadRequest)
  }

  def speakerViewAgain(id: Long) = Action { request =>
    LiveStream.findById(id) match {
      case Some(l: LiveStream) => {
        Ok(views.html.speaker(l.id.get, l.url))
      }
      case None => {
        BadRequest
      }
    }
  }

}
