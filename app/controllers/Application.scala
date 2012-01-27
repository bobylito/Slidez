package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {
  
  def index = Action {
    list()
  }
  
  def list = Action { request => 
    //TODO response
  }
  
  def initPrez = Action { request =>
    //announced Prez
  }
  
  def broadCast = Action { request =>
    //To the clients 
  }
  
  def listen = Action { request =>
      //Client content fetching 
  }
  
  def startPrez = Action { request =>
    //Push state to the client 
  }
  
  def  stopPrez = Action { request =>
    //Push state to the client and archive 
  }
}