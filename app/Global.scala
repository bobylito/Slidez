import play.api._

object Global extends GlobalSettings{

  override def onStart(app : Application){
    Logger.info("Let's get sliding!");  
  }

  override def onStop(app : Application){
    Logger.info("Oh noooo!");  
  }
}
