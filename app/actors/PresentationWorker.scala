package actors

import akka.actor._
import akka.actor.Actor._
import play.api.libs.iteratee._
import play.api.libs.iteratee.Enumerator._
import play.libs.Akka
import scala.collection.mutable.Set

class PresentationWorker extends Actor {
  import PresentationWorker._

  var audiences: Map[Long, Set[PushEnumerator[String]]] = Map()

  def receive = {
    case Listen(id) => {
      audiences.get(id).map( audience => {
        lazy val channel : PushEnumerator[String] = Enumerator.imperative( 
          onStart = {audience += channel},
          onComplete = { self ! Quit(channel)}, 
          onError = (a, b) => {  println("Error") }
        )
        sender ! channel
      })
    }
    case ChangePage(id, page) => {
      audiences.get(id).map( audience => {
        audience.foreach( p => {
            p.push(page)
          })
        })
    }
    case Quit(quitter) => {
      quitter.close()
      println("Quit!")
    }
    case NewPresentation(id) => {
      audiences += (id -> Set.empty[PushEnumerator[String]])
    }
  }
}

object PresentationWorker {
  sealed trait Message

  case class Listen(id: Long) extends Message
  case class Quit(quitter : PushEnumerator[String]) extends Message
  case class ChangePage(id: Long, page: String) extends Message
  case class NewPresentation(id: Long) extends Message

  lazy val ref = Akka.system.actorOf(Props[PresentationWorker])
}
