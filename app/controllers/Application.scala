package controllers

import play.api._
import play.api.mvc._
import models.LiveStream
import scala.collection.immutable 

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

  def broadCast = Action { request =>
    //To the clients 
    Ok("broadCast")
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

  def view = Action{ request => 
    Ok("Welcome")
  }

  def speakerView = Action{ request => 
    request.body.asFormUrlEncoded.get("url")
        .headOption
        .map(url => Ok(views.html.speaker(5, url)))
        .getOrElse(BadRequest)

  }
}
