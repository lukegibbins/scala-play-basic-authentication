
# --- !Ups
create TABLE address (
  addressID             int NOT NULL AUTO_INCREMENT,
  houseNo               INT,
  line1                 VARCHAR(255) not null,
  line2                 VARCHAR(255),
  postcode              VARCHAR(9) not NULL,
  city                  VARCHAR(255) not NULL,
  county                varchar(255) not null,
  PRIMARY KEY (addressID)
);

create TABLE user (
  email                   varchar(255) not NULL,
  guid                    varchar(255),
  password                varchar(255) not NULL,
  name                    varchar(255) not NULL,
  surname                 VARCHAR(255) not NULL,
  DOB                     DATE NOT NULL,
  addressID               INT NOT NULL,
  gender                  VARCHAR(10) not NULL,
  tel                     BIGINT not NULL,
  image                   VARCHAR(255),
  PRIMARY KEY (email),
  FOREIGN KEY (addressID) REFERENCES address(addressID)
);


INSERT INTO address(addressID, houseNo, line1, line2, postcode, city, county)
VALUES (0,49,'Pleasant Park','Freddy Street','F0R TNIT3','Newcastle','Mordor');

INSERT INTO address(addressID, houseNo, line1, line2, postcode, city, county)
VALUES (1,49,'Tilted Towers','John Street','F0R TNIT3','Sunderland','The Shire');

INSERT INTO user (email,guid, password, name, surname, DOB, addressID, gender, tel, image)
VALUES ('user@user.com',NULL,'pass','Marcus','Phoenix',TO_DATE('03/07/1994', 'DD/MM/YYYY'), 0, 'Male', 123, NULL);

INSERT INTO user (email, guid, password, name, surname, DOB, addressID, gender, tel, image)
VALUES ('test@test.com',NULL ,'pass','Dom','Santiago',TO_DATE('13/02/1991', 'DD/MM/YYYY'), 1, 'Female', 123, NULL);



# --- !Downs
drop table if EXISTS address;
drop table if EXISTS user;


