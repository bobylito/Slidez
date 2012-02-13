package controllers.lib 

import play.api.mvc._
import play.api.libs.iteratee._
import play.api.templates._
/**
 * Helper function to produce a "Server Sent Events" Enumeratee.
 */
object SSEvent {

  /**
   * Create a SSE Enumeratee.
   *
   * @tparam E Type of messages handled by this comet stream.
   * @param eventName is the event type sent by the server.
   * @param initialChunck hack for the browser to not close the connection
   */
  def apply[E](eventName: String) = new Enumeratee[E, String] {
    def applyOn[A](inner: Iteratee[String, A]): Iteratee[E, Iteratee[String, A]] = {
      val eToSSEvent = Enumeratee.map[E](data => {
            "event: " + eventName + "\ndata: " + data.toString() + "\n\n"
          })
      eToSSEvent.applyOn(inner)
    }
  }
}
