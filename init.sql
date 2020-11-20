DROP DATABASE IF EXISTS blackjack;
CREATE DATABASE blackjack;
use blackjack;

create table Users (
	id int not null primary key AUTO_INCREMENT,
  username varchar(255) not null, 
  password BINARY(40), 
  tokens DOUBLE
  );
  
insert into Users (username, password, tokens) values 
	('testuser1', SHA1('password'), 10),
	('testuser2', SHA1('password'), 13),
	('testuser3', SHA1('password'), 0),
	('testuser4', SHA1('password'), 1361),
	('testuser5', SHA1('password'), 1124),
	('testuser6', SHA1('password'), 100);