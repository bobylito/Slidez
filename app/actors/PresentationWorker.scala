package actors

import akka.actor._
import akka.actor.Actor._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Enumerator._
import play.libs.Akka
import scala.collection.mutable.Set

class PresentationWorker extends Actor {
  import PresentationWorker._

  var audiences: Map[Long, Set[Pushee[String]]] = Map()

  def receive = {
    case Listen(id) => {
      audiences.get(id) match {
        case Some(audience) => {
          lazy val channel : Enumerator[String] = Enumerator.pushee(
            pushee => {
              audience += pushee 
            }, onComplete = self ! Quit)
          sender ! channel
        }
        case None => {

        }
      }
    }
    case ChangePage(id, page) => {
      audiences.get(id) match {
        case Some(audience) => {
          audience.foreach(_.push(page))
        }
        case None => {

        }
      }
    }
    case Quit => {
      println("Quit!")
    }
    case NewPresentation(id) => {
      audiences += (id -> Set.empty[Pushee[String]])
    }
  }
}

object PresentationWorker {
  sealed trait Message

  case object Quit extends Message

  case class Listen(id: Long) extends Message
  case class ChangePage(id: Long, page: String) extends Message
  case class NewPresentation(id: Long) extends Message

  lazy val ref = Akka.system.actorOf(Props[PresentationWorker])
}
