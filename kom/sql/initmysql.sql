CREATE TABLE names
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
CREATE TABLE users 
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
	flags BIGINT NOT NULL,
	rights BIGINT NOT NULL,
	locale VARCHAR(100),
	charset VARCHAR(50) NOT NULL,
	url VARCHAR(200),
	PRIMARY KEY(id),
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX userid_ix(userid),
) TYPE=INNODB;
CREATE TABLE conferences 
(
	id BIGINT NOT NULL,
	administrator BIGINT NOT NULL,
	replyConf BIGINT,
	permissions INT NOT NULL,
	nonmember_permissions INT NOT NULL,
	PRIMARY KEY(id),	
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX admin_ix(administrator),
	FOREIGN KEY (administrator) REFERENCES users(id) ON DELETE RESTRICT,
	INDEX replyConf_ix(replyConf),
	FOREIGN KEY (replyConf) REFERENCES conferences(id) ON DELETE SET NULL
) TYPE=INNODB;
CREATE TABLE memberships
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
CREATE TABLE messages
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	created TIMESTAMP NOT NULL,
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
CREATE TABLE messageoccurrences
(
	localnum INT NOT NULL,
	conference BIGINT NOT NULL,
	message BIGINT NOT NULL,
	action_ts TIMESTAMP NOT NULL,
	kind SMALLINT NOT NULL,
	user BIGINT,
	user_name VARCHAR(80),
	PRIMARY KEY(conference, localnum),
	INDEX occconf_ix(conference),
	FOREIGN KEY (conference) REFERENCES conferences(id) ON DELETE CASCADE,
	INDEX occuser_ix(user),
	FOREIGN KEY (user) REFERENCES users(id) ON DELETE SET NULL,
	INDEX occmessage_ix(message),
	FOREIGN KEY (message) REFERENCES messages(id) ON DELETE CASCADE,
) TYPE=INNODB;