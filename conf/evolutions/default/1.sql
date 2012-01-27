# Slidez schema

# --- !Ups

CREATE TABLE LIVESTREAM (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (id)
)

# --- !Downs

DROP TABLE LIVESTREAM
