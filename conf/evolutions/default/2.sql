# --- !Ups

CREATE TABLE LOGSLIDE (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  prezid bigint(20) NOT NULL, 
  slide varchar(10) NOT NULL,
  tick date NOT NULL, 
  PRIMARY KEY (id),
  FOREIGN KEY (prezid) REFERENCES LIVESTREAM (id)
)

# --- !Downs

DROP TABLE LOGSLIDE
