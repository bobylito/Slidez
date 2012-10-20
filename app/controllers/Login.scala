package controllers

import play.api._
import play.api.mvc._
import play.api.mvc.Security.Authenticated
import play.api.libs.ws.WS

import java.net.URLEncoder


object Login extends Controller with Secured{

  def verifyAssertion = Action { request => 
    request.body.asFormUrlEncoded.get("assertion").headOption.map(
        assertion => {
          //TODO Verify assertion
          Async{
            WS.url("https://verifier.login.persona.org/verify")
              .withQueryString(
                ("assertion", assertion), 
                ("audience", "http://localhost:9001"))
              .post("")
              .map({ response => 
                if(response.status == 200){
                  (response.json \ "status").asOpt[String] match {
                    case Some( "okay")  => {
                      (response.json \ "email" ).asOpt[String].map( { mail => 
                          Ok(response.json).withSession(request.session + ("user.email" -> mail))
                        } ).getOrElse( {
                          BadRequest  
                        } )
                    }
                    case Some("failure") => {
                      BadRequest(response.json)
                    }
                    case _ => {
                      BadRequest("Erreur")
                    }
                  }
                }
                else {
                  BadRequest("Something went wrong with the persona ws")
                }
              })
          }
        }
    )
    .getOrElse(BadRequest)
  }

  def test =  isAuthenticated( (user, request) => Ok("Hello "+ user))

  def signout = Action(request => 
        Redirect(routes.Application.index()).withNewSession 
      )
}

trait Secured {
  private def getUsername(req : RequestHeader) = req.session.get("user.email")
  private def notForYou(req : RequestHeader) = Results.Redirect(routes.Application.index)

  def isAuthenticated( f : (String, Request[AnyContent]) => Result) = Security.Authenticated(getUsername, notForYou)({
    user => Action(request => f(user, request))
  })

  def optionAuthenticated(f : (Option[String], Request[AnyContent]) => Result) = {
    Action(request => f(getUsername(request), request))
  }
}
