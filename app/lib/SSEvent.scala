package controllers.lib 

import play.api.mvc._
import play.api.libs.iteratee._
import play.api.templates._

import org.apache.commons.lang.{ StringEscapeUtils }

/**
 * Helper function to produce a "Server Sent Events" Enumeratee.
 *
 * Really it is a quite ripoff of play2's comet implem' :P
 */
object SSEvent {

  case class SSMessage[A](toJavascriptMessage: A => String)

  object SSMessage {

    /**
     * String messages.
     */
    implicit val stringMessages = SSMessage[String](str => StringEscapeUtils.escapeJavaScript(str))

    /**
     * Json messages.
     */
    implicit val jsonMessages = SSMessage[play.api.libs.json.JsValue](json => json.toString())
  }

  import play.Logger
  /**
   * Create a SSE Enumeratee.
   *
   * @tparam E Type of messages handled by this comet stream.
   * @param eventName is the event type sent by the server.
   * @param initialChunck hack for the browser to not close the connection
   */
  def apply[E](eventName: String, initialChunck: String = Array.fill[Char](5 * 1024)(' ').mkString + "\n")(implicit encoder: SSMessage[E]) = new Enumeratee[E, String] {
    def applyOn[A](inner: Iteratee[String, A]): Iteratee[E, Iteratee[String, A]] = {
      val fedWithInitialChunk = Iteratee.flatten(Enumerator(initialChunck) |>> inner)
      val eToSSEvent = Enumeratee.map[E](data => {
            "event: " + eventName + "\ndata: " + encoder.toJavascriptMessage(data) + "\n\n"
          })
      eToSSEvent.applyOn(fedWithInitialChunk)
    }
  }
}
