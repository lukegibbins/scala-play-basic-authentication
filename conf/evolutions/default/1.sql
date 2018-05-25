
# --- !Ups
create TABLE contacts (
  id                  bigint not null AUTO_INCREMENT,
  name                varchar(255),
  email               varchar(255),
  PRIMARY KEY (id)
);

INSERT INTO contacts (id, name, email) VALUES (101,'Samwise','bob@gmail.com');
INSERT INTO contacts (id, name, email) VALUES (102,'Harry','sk21@gmail.com');
INSERT INTO contacts (id, name, email) VALUES (103,'Frodo','lm27@gmail.com');


# --- !Downs
drop table if EXISTS contacts

