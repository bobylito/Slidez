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
      {case (id, page) => println("Page actuelle" + page)
                          PresentationWorker.ref ! ChangePage(page)
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
      (PresentationWorker.ref ? Listen).mapTo[Enumerator[String]].asPromise.map({chunks =>{
        Ok.stream(chunks &> eventEnum)(writeable = Writeable.wString, contentTypeOf = ContentTypeOf[String](Some(ContentTypes.EVENT_STREAM)))
      } })
    }
  }  

  def view = Action{ request => 
    Ok(views.html.view())
  }

  def speakerView = Action{ request => 
    request.body.asFormUrlEncoded.get("url")
        .headOption
        .map(url => Ok(views.html.speaker(5, url)))
        .getOrElse(BadRequest)
  }


  lazy val toto : Enumerator[String] = {
    Enumerator.fromCallback { () => 
      Promise.timeout(Some("toto"), 100 milliseconds)    
    }
  }


  def liveToto = Action {
    Ok.stream(toto &> SSEvent(eventName = "totoEvent"))(writeable = Writeable.wString, contentTypeOf =
      ContentTypeOf[String](Some(ContentTypes.EVENT_STREAM)))
  }
}
