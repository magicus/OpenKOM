CREATE DATABASE IF NOT EXISTS kom;

GRANT SELECT, INSERT, DELETE, UPDATE, CREATE, ALTER, INDEX, DROP ON kom.* TO 'kom' IDENTIFIED BY 'kom';

FLUSH PRIVILEGES;

USE kom;

CREATE TABLE IF NOT EXISTS names
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	norm_name VARCHAR(80) NOT NULL,
	fullname VARCHAR(80) NOT NULL,
	emailalias VARCHAR(80),
	kind SMALLINT,
	visibility SMALLINT NOT NULL,
	keywords VARCHAR(100),	
	PRIMARY KEY(id),
	UNIQUE INDEX norm_name_ix(norm_name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS users 
(
	id BIGINT NOT NULL,
	userid VARCHAR(50),
	pwddigest VARCHAR(100),
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
	timezone VARCHAR(100),
	charset VARCHAR(50) NOT NULL,
	url VARCHAR(200),
	created DATETIME NOT NULL,
	lastlogin DATETIME NOT NULL,
	PRIMARY KEY(id),
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX userid_ix(userid)
) ENGINE=INNODB;

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
) ENGINE=INNODB;

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
) ENGINE=INNODB;

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
	body LONGTEXT NOT NULL,
	PRIMARY KEY(id),
	INDEX msgauthor_ix(author),
	FOREIGN KEY (author) REFERENCES users(id) ON DELETE SET NULL,
	INDEX msgreply_ix(reply_to),
	FOREIGN KEY (reply_to) REFERENCES messages(id) ON DELETE SET NULL,
	INDEX thread_ix(thread),
	INDEX msg_author_created(author, created)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS messagesearch 
(
	id BIGINT(20) NOT NULL,
	subject VARCHAR(80) NOT NULL,
	body LONGTEXT NOT NULL,
	PRIMARY KEY (id),
	FULLTEXT KEY search_ix (subject,body)
) ENGINE=MyISAM; 

CREATE TABLE IF NOT EXISTS messageoccurrences
(
	localnum INT NOT NULL,
	conference BIGINT NOT NULL,
	message BIGINT,
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
) ENGINE=INNODB;

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
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS bookmarks
(
	user BIGINT NOT NULL,
	message BIGINT NOT NULL,
	created DATETIME NOT NULL,
	annotation VARCHAR(100),
	PRIMARY KEY(user, message),
	FOREIGN KEY(message) REFERENCES messages(id) ON DELETE CASCADE,
	INDEX(user, created)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS magicconferences
(
	user BIGINT NOT NULL,
	conference BIGINT NULL,
	kind SMALLINT NOT NULL,
	PRIMARY KEY(conference, kind),
	FOREIGN KEY(conference) REFERENCES conferences(id) ON DELETE CASCADE,
	FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=INNODB;

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
) ENGINE=INNODB;

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
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS userlog
(
	user BIGINT NOT NULL,
	logged_in DATETIME NOT NULL,
	logged_out DATETIME NOT NULL,
	num_posted INT NOT NULL,
	num_read INT NOT NULL,
	num_chat_messages INT NOT NULL,
	num_broadcasts INT NOT NULL,
	num_copies INT NOT NULL,
	INDEX userlog_user(user),
	FOREIGN KEY (user) REFERENCES users(id) ON DELETE CASCADE,
	INDEX userlog_date(logged_in)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS files
(
	parent BIGINT NOT NULL,
	name VARCHAR(100) NOT NULL,
	created DATETIME NOT NULL,
	updated DATETIME NOT NULL,
	content TEXT NOT NULL,
	protection INT NOT NULL,
	PRIMARY KEY(parent, name),
	INDEX file_parentix(parent), 
	FOREIGN KEY (parent) REFERENCES names(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS settings
(
	name VARCHAR(100) NOT NULL,
	string_value VARCHAR(100),
	numeric_value BIGINT,
	PRIMARY KEY(name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS relationships
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	referer BIGINT NOT NULL,
	referee BIGINT NOT NULL,
	kind INT NOT NULL,
	flags BIGINT NOT NULL,
	PRIMARY KEY(id),
	INDEX referer_ix(referer),
	FOREIGN KEY (referer) REFERENCES names(id) ON DELETE CASCADE,
	INDEX referee_ix(referee),
	FOREIGN KEY (referee) REFERENCES names(id) ON DELETE CASCADE
) ENGINE=INNODB;

-- Create sysop
--
INSERT INTO names
	(id, norm_name, fullname, kind, visibility) 
VALUES 
	(1, 'SYSOP', 'Sysop', 0, 0);

INSERT INTO users
	(id, userid, pwddigest, flags1, rights, locale, charset, created)
VALUES
	(1, 'sysop', '$1$7jv0PRm9$sBErMtzDqq33l2WByDWEc1', 124239, -1, 'sv_SE', 'ISO-8859-1', NOW());
INSERT INTO conferences
	(id, administrator, replyConf, permissions, nonmember_permissions, created)
VALUES
	(1, 1, null, 0, 0, NOW());
INSERT INTO memberships
	(conference, user, priority, flags, active, permissions, negation_mask, markers)
VALUES
	(1, 1, 1, 0, 1, 32727, 0, null);
