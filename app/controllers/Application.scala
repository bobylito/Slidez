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

object Application extends Controller with Secured {

  import akka.pattern.ask

  /** 
   *  INDEX 
   */
  def index = optionAuthenticated( { (user, request) => 
    //live stream list 
    val streams = LiveStream.findAll()
    Ok(views.html.index(streams, user))
  })
  
  def secret = optionAuthenticated( { (user, request) => 
    //live stream list 
    val streams = LiveStream.findAll()
    Ok(views.html.secret(streams, user))
  })

/**
 * Creates the database entry and display the slides
 */
  def speakerView = Action { request =>
    request.body.asFormUrlEncoded.get("url")
      .headOption
      .map(url => {
        LiveStream.create( LiveStream(NotAssigned, "Presentation hackday slidez", url) )
          .map(id => {
            PresentationWorker.ref ! NewPresentation(id)
            //Ok(views.html.speaker(id, url))
            Redirect(routes.Application.speakerViewAgain(id))
          })
          .getOrElse( BadRequest )
        })
      .getOrElse( BadRequest )
  }

  /**
   * Restore a previously entered presentation
   */
  def speakerViewAgain(id: Long) = optionAuthenticated( (user, request) =>
    LiveStream.findById(id)
      .map( l => {
        Ok(views.html.speaker(l.id.get, l.url, user))
      })
      .getOrElse(BadRequest )
  )
  
  val broadcastForm = Form(
    tuple(
      "id" -> number,
      "page" -> nonEmptyText,
      "tick" -> number))

  /**
   * Update the state of the slides and broadcast the info to listeners
   */
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

  def updateSlidesName(id: Long, name: String) = Action { 
    LiveStream.update(id, name)
    Ok("Updated")
  }

  def view(id: Long) = optionAuthenticated( (user, request) =>
    LiveStream.findById(id)
      .map(l => {Ok(views.html.view(l.url, l.id.get, LogSlide.lastShownSlide(id), user))})
      .getOrElse( { BadRequest })
  )

  val eventEnum = SSEvent[String](eventName = "page-change")

  def viewStream(id: Long) = Action {
    LiveStream.findById(id) match {
      case Some(l: LiveStream) => {
        AsyncResult {
          implicit val timeout = Timeout(5 second)
          (PresentationWorker.ref ? Listen(id) ).mapTo[Enumerator[String]].asPromise.map({ in =>
            Ok.stream( in &> eventEnum )
              .withHeaders( 
                ( CONTENT_LENGTH, "-1"),
                ( CACHE_CONTROL, "no-cache"),
                ( CONTENT_TYPE, "text/event-stream"))
          })
        }
      }
      case None => {
        BadRequest("")
      }
    }
  }
}
