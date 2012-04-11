package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS

import java.net.URLEncoder


object Login extends Controller {

  def verifyAssertion = Action { request => 
    request.body.asFormUrlEncoded.get("assertion").headOption.map(
        assertion => {
          //TODO Verify assertion
          Async{
            WS.url("https://browserid.org/verify")
              .withQueryString(
                ("assertion", assertion), 
                ("audience", "http://localhost:9000"))
              .post("")
              .map {
                response => {
                  Logger.info(response.status.toString())
                  Logger.info(response.json.toString())
                  Ok(response.json)
              }
            }
          }
        }
      ).getOrElse(BadRequest)
  }

}
