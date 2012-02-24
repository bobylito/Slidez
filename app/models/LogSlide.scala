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
      SQL("insert into logslide(prezid, slide, tick) set({prezid}, {slide}, {tick})")
          .on(
              'prezid -> log.prezid,
              'slide -> log.slide,
              'tick -> log.tick
          ).executeInsert()
    }
  }
  
}