package models
import play.api.db._
import anorm._
import anorm.SqlParser._
import play.api.Play.current

case class LiveStream (id: Pk[Long],
  name : String, 
  url : String
)

object LiveStream {

    val simple = {
      val rs = 
        (get[Pk[Long]]("livestream.id") ~
        get[String]("livestream.name") ~
        get[String]("livestream.url"))map {
          case id ~ name ~ url
            => (id, name,  url)
        } *; 

      rs.map(r =>
        r.groupBy(_._1)
        .flatMap{ case (k, ps) =>
            ps.headOption.map{ p =>
                val (id, name, url) = p
                LiveStream(id, name, url)
            }
        }.toList
    )
    
  }

  
  def findAll : Seq[LiveStream] = {
    DB.withConnection { implicit connection => 
      SQL(""" 
          select * from livestream
      	  """).as(LiveStream.simple)
    }
  }
  
  def findById(id: Long): Option[LiveStream] = {
    DB.withConnection { implicit connection =>
      SQL("""
        select * from livestream
        where id = {id}
         """).on(
        'id -> id
      ).as(LiveStream.simple).headOption
    }
  }
  
  
    def create(livestream: LiveStream): Int = {
     //TODO : id generator
     DB.withTransaction { implicit connection =>
       SQL(
         """
           insert into livestream values (
             NULL, {name}, {url}
           )
         """
       ).on(
         'name -> livestream.name,
         'url -> livestream.url
       ).executeUpdate()
     }
  }

}