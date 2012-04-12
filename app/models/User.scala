package models 

import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

case class User(
    id : Pk[Long],
    mail : String,
    name : String,
    twitterAccount : String
  )
/*
object User {
  val simple = {
    val rs = (
        get[Pk[Long]]("user.id"),
        get[String]("user.mail"),
        get[String]("user.name"),
        get[String]("user.twitterAccount"),
      ) map {
      case id ~ mail ~ name ~ twitterAccount => (id, mail, name, twitterAccount)
    } *;

    rs.map(
      r => 
        r.groupBy(_._1).
          .flatMap {
            case(k, ps) => 
              ps.headOption.map {p => 
                val (id, mail, name, twitterAccount) = p
                User(id, mail, name, twitterAccount)
              }
          }
          .toList)
  }
  
  def register(mail: String) : Option[Long] = {
    DB.withTransaction{ 
      implicit connection => 
        SQL(
          """ 
          INSERT INTO user (mail)
          """)
            .on('mail -> mail)
            .executeInsert()
    }
  }
}
  */
