CREATE DATABASE IF NOT EXISTS kom;

GRANT SELECT, INSERT, DELETE, UPDATE, CREATE, ALTER, INDEX, DROP ON kom.* TO 'kom' IDENTIFIED BY 'kom';

FLUSH PRIVILEGES;

USE kom;

CREATE TABLE IF NOT EXISTS names
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	norm_name VARCHAR(80) NOT NULL,
	fullname VARCHAR(80) NOT NULL,
	nickname VARCHAR(80),
	kind SMALLINT,
	visibility SMALLINT NOT NULL,
	PRIMARY KEY(id),
	UNIQUE INDEX norm_name_ix(norm_name)
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS users 
(
	id BIGINT NOT NULL,
	userid VARCHAR(50),
	pwddigest BINARY(50),
	address1 VARCHAR(50),
	address2 VARCHAR(50),
	address3 VARCHAR(50),
	address4 VARCHAR(50),
	phoneno1 VARCHAR(50),
	phoneno2 VARCHAR(50),
	email1 VARCHAR(200),
	email2 VARCHAR(200),
	flags1 BIGINT NOT NULL,
	flags2 BIGINT NOT NULL,
	flags3 BIGINT NOT NULL,
	flags4 BIGINT NOT NULL,
	rights BIGINT NOT NULL,
	locale VARCHAR(100),
	charset VARCHAR(50) NOT NULL,
	url VARCHAR(200),
	created DATETIME NOT NULL,
	lastlogin DATETIME NOT NULL,
	PRIMARY KEY(id),
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX userid_ix(userid)
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS conferences 
(
	id BIGINT NOT NULL,
	administrator BIGINT NOT NULL,
	replyConf BIGINT,
	permissions INT NOT NULL,
	nonmember_permissions INT NOT NULL,
	created DATETIME NOT NULL,
	lasttext DATETIME NOT NULL,
	PRIMARY KEY(id),	
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX admin_ix(administrator),
	FOREIGN KEY (administrator) REFERENCES users(id) ON DELETE RESTRICT,
	INDEX replyConf_ix(replyConf),
	FOREIGN KEY (replyConf) REFERENCES conferences(id) ON DELETE SET NULL,
    INDEX lasttext_ix(lasttext)
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS memberships
(
	conference BIGINT NOT NULL,
	user BIGINT NOT NULL,
	priority BIGINT NOT NULL,
	flags BIGINT NOT NULL,
	active BIT,
	permissions INT NOT NULL,
	negation_mask INT NOT NULL,	
	markers TEXT,
	PRIMARY KEY(conference, user),
	INDEX mbrconf_ix(conference),
	FOREIGN KEY (conference) REFERENCES conferences(id) ON DELETE CASCADE,
	INDEX mbruser_ix(user),
	FOREIGN KEY (user) REFERENCES users(id) ON DELETE CASCADE		
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS messages
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	created DATETIME NOT NULL,
	author BIGINT,
	author_name VARCHAR(80),
	reply_to BIGINT,
	thread BIGINT,
	kind SMALLINT NOT NULL,
	subject VARCHAR(80) NOT NULL,
	body TEXT NOT NULL,
	PRIMARY KEY(id),
	INDEX msgauthor_ix(author),
	FOREIGN KEY (author) REFERENCES users(id) ON DELETE SET NULL,
	INDEX msgreply_ix(reply_to),
	FOREIGN KEY (reply_to) REFERENCES messages(id) ON DELETE SET NULL
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS messageoccurrences
(
	localnum INT NOT NULL,
	conference BIGINT NOT NULL,
	message BIGINT NOT NULL,
	action_ts DATETIME NOT NULL,
	kind SMALLINT NOT NULL,
	user BIGINT,
	user_name VARCHAR(80),
	PRIMARY KEY(conference, localnum),
	INDEX occconf_ix(conference),
	FOREIGN KEY (conference) REFERENCES conferences(id) ON DELETE CASCADE,
	INDEX occuser_ix(user),
	FOREIGN KEY (user) REFERENCES users(id) ON DELETE SET NULL,
	INDEX occmessage_ix(message),
	FOREIGN KEY (message) REFERENCES messages(id) ON DELETE CASCADE
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS messageattributes
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	message BIGINT NOT NULL,
	kind SMALLINT NOT NULL,
	created DATETIME NOT NULL,
	value TEXT,
	PRIMARY KEY (id),
	INDEX message_ix(message),
	FOREIGN KEY (message) REFERENCES messages(id) ON DELETE CASCADE,
	INDEX messagekind_ix(message, kind)
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS magicconferences
(
	conference BIGINT NOT NULL,
	kind SMALLINT NOT NULL,
	PRIMARY KEY(conference, kind),
	FOREIGN KEY (conference) REFERENCES conferences(id) ON DELETE CASCADE
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS messagelog
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	body TEXT NOT NULL,
	created DATETIME NOT NULL,
	author BIGINT,
	author_name VARCHAR(80) NOT NULL,
	PRIMARY KEY(id),
	INDEX mlauthor(author),
	FOREIGN KEY (author) REFERENCES users(id) ON DELETE SET NULL
) TYPE=INNODB;

CREATE TABLE IF NOT EXISTS messagelogpointers
(
	recipient BIGINT NOT NULL,
	logid BIGINT NOT NULL,
	kind SMALLINT NOT NULL,
	sent BIT NOT NULL,
	INDEX mlpuser_ix(recipient),
	FOREIGN KEY (recipient) REFERENCES names(id) ON DELETE CASCADE,
	INDEX logitem(logid),
	FOREIGN KEY (logid) REFERENCES messagelog(id) ON DELETE CASCADE
) TYPE=INNODB;