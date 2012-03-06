package models

import java.sql.Time
import play.api.db._
import play.api.Play.current
import anorm._
import anorm.SqlParser._

case class LogSlide(id: Pk[Long], prezid: Long, slide: String, tick: Long)

object LogSlide {

  def insert(log: LogSlide) : Option[Long]= {
    DB.withConnection { implicit connection => 
      SQL("insert into logslide(prezid, slide, tick) VALUES ({prezid}, {slide}, {tick})")
          .on(
              'prezid -> log.prezid,
              'slide -> log.slide,
              'tick -> new Time(log.tick)
          ).executeInsert()
    }
  }
  
  def lastShownSlide(id : Long) : Option[String] = {
    DB.withConnection { implicit connection => 
      SQL( "select slide from logslide where prezid={id} and tick = (select max(tick) from logslide where prezid={id}) " )
          .on(
              'id -> id
          ).as(scalar[String].singleOpt)
    }
  }
  
}