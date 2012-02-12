package actors

import akka.actor._
import akka.actor.Actor._

import play.api.libs.iteratee._
import play.api.libs.iteratee.Enumerator._

import play.libs.Akka

class PresentationWorker extends Actor{
  import PresentationWorker._

  var audience : List[Pushee[String]] = Nil

  def receive = {
    case Listen => {
      lazy val channel : Enumerator[String] = Enumerator.pushee(
          pushee => self ! Init(pushee), onComplete = self ! Quit
        )
      sender ! channel
    }
    case Init(pushee) => {
      audience = audience :+ pushee
    }
    case ChangePage(page) => {
      audience.foreach(_.push(page))
    }
    case Quit => { 
      //Rien
    }
  }
}

object PresentationWorker {
  sealed trait Message 
  
  case object Listen extends Message
  case object Quit extends Message

  case class Init(p: Pushee[String]) extends Message
  case class ChangePage(page: String) extends Message

  lazy val ref = Akka.system.actorOf(Props[PresentationWorker])
}
