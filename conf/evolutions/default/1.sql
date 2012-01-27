# Slidez schema

# --- !Ups

CREATE TABLE LIVESTREAM (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255),
  url varchar(255),
  state int(5),
  PRIMARY KEY (id)
)

# --- !Downs

DROP TABLE LIVESTREAM
