CREATE DATABASE IF NOT EXISTS kom;

GRANT SELECT, INSERT, DELETE, UPDATE, CREATE, ALTER, INDEX, DROP ON kom.* TO 'kom' IDENTIFIED BY 'kom';

FLUSH PRIVILEGES;

USE kom;

CREATE TABLE IF NOT EXISTS names
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	norm_name VARCHAR(80) NOT NULL DEFAULT '',
	fullname VARCHAR(80) NOT NULL DEFAULT '',
	emailalias VARCHAR(80),
	kind SMALLINT,
	visibility SMALLINT NOT NULL DEFAULT 0,
	keywords VARCHAR(100),	
	PRIMARY KEY(id),
	UNIQUE INDEX norm_name_ix(norm_name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS users 
(
	id BIGINT NOT NULL DEFAULT 0,
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
	flags1 BIGINT NOT NULL DEFAULT 0,
	flags2 BIGINT NOT NULL DEFAULT 0,
	flags3 BIGINT NOT NULL DEFAULT 0,
	flags4 BIGINT NOT NULL DEFAULT 0,
	rights BIGINT NOT NULL DEFAULT 0,
	locale VARCHAR(100),
	timezone VARCHAR(100),
	charset VARCHAR(50) NOT NULL DEFAULT '',
	url VARCHAR(200),
	created DATETIME NOT NULL DEFAULT NOW(),
	lastlogin DATETIME NOT NULL DEFAULT NOW(),
	PRIMARY KEY(id),
	FOREIGN KEY (id) REFERENCES names(id) ON DELETE CASCADE,
	INDEX userid_ix(userid)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS conferences 
(
	id BIGINT NOT NULL DEFAULT 0,
	administrator BIGINT NOT NULL DEFAULT 0,
	replyConf BIGINT,
	permissions INT NOT NULL DEFAULT 0,
	nonmember_permissions INT NOT NULL DEFAULT 0,
	created DATETIME NOT NULL DEFAULT NOW(),
	lasttext DATETIME NOT NULL DEFAULT NOW(),
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
	conference BIGINT NOT NULL DEFAULT 0,
	user BIGINT NOT NULL DEFAULT 0,
	priority BIGINT NOT NULL DEFAULT 0,
	flags BIGINT NOT NULL DEFAULT 0,
	active BOOL DEFAULT 0,
	permissions INT NOT NULL DEFAULT 0,
	negation_mask INT NOT NULL DEFAULT 0,
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
	created DATETIME NOT NULL DEFAULT NOW(),
	author BIGINT,
	author_name VARCHAR(80),
	reply_to BIGINT,
	thread BIGINT,
	kind SMALLINT NOT NULL DEFAULT 0,
	subject VARCHAR(80) NOT NULL DEFAULT '',
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
	id BIGINT(20) NOT NULL DEFAULT 0,
	subject VARCHAR(80) NOT NULL DEFAULT '',
	body LONGTEXT NOT NULL,
	PRIMARY KEY (id),
	FULLTEXT KEY search_ix (subject,body)
) ENGINE=MyISAM; 

CREATE TABLE IF NOT EXISTS messageoccurrences
(
	localnum INT NOT NULL DEFAULT 0,
	conference BIGINT NOT NULL DEFAULT 0,
	message BIGINT,
	action_ts DATETIME NOT NULL DEFAULT NOW(),
	kind SMALLINT NOT NULL DEFAULT 0,
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
	message BIGINT NOT NULL DEFAULT 0,
	kind SMALLINT NOT NULL DEFAULT 0,
	created DATETIME NOT NULL DEFAULT NOW(),
	value TEXT,
	PRIMARY KEY (id),
	INDEX message_ix(message),
	FOREIGN KEY (message) REFERENCES messages(id) ON DELETE CASCADE,
	INDEX messagekind_ix(message, kind)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS bookmarks
(
	user BIGINT NOT NULL DEFAULT 0,
	message BIGINT NOT NULL DEFAULT 0,
	created DATETIME NOT NULL DEFAULT NOW(),
	annotation VARCHAR(100),
	PRIMARY KEY(user, message),
	FOREIGN KEY(message) REFERENCES messages(id) ON DELETE CASCADE,
	INDEX(user, created)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS magicconferences
(
	user BIGINT NOT NULL DEFAULT 0,
	conference BIGINT NULL DEFAULT 0,
	kind SMALLINT NOT NULL DEFAULT 0,
	PRIMARY KEY(conference, kind),
	FOREIGN KEY(conference) REFERENCES conferences(id) ON DELETE CASCADE,
	FOREIGN KEY(user) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS messagelog
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	body TEXT NOT NULL,
	created DATETIME NOT NULL DEFAULT NOW(),
	author BIGINT,
	author_name VARCHAR(80) NOT NULL DEFAULT '',
	PRIMARY KEY(id),
	INDEX mlauthor(author),
	FOREIGN KEY (author) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS messagelogpointers
(
	recipient BIGINT NOT NULL DEFAULT 0,
	logid BIGINT NOT NULL DEFAULT 0,
	kind SMALLINT NOT NULL DEFAULT 0,
	sent BOOL NOT NULL DEFAULT 0,
	INDEX mlpuser_ix(recipient),
	FOREIGN KEY (recipient) REFERENCES names(id) ON DELETE CASCADE,
	INDEX logitem(logid),
	FOREIGN KEY (logid) REFERENCES messagelog(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS userlog
(
	user BIGINT NOT NULL DEFAULT 0,
	logged_in DATETIME NOT NULL DEFAULT NOW(),
	logged_out DATETIME NOT NULL DEFAULT NOW(),
	num_posted INT NOT NULL DEFAULT 0,
	num_read INT NOT NULL DEFAULT 0,
	num_chat_messages INT NOT NULL DEFAULT 0,
	num_broadcasts INT NOT NULL DEFAULT 0,
	num_copies INT NOT NULL DEFAULT 0,
	INDEX userlog_user(user),
	FOREIGN KEY (user) REFERENCES users(id) ON DELETE CASCADE,
	INDEX userlog_date(logged_in)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS files
(
	parent BIGINT NOT NULL DEFAULT 0,
	name VARCHAR(100) NOT NULL DEFAULT '',
	created DATETIME NOT NULL DEFAULT NOW(),
	updated DATETIME NOT NULL DEFAULT NOW(),
	content TEXT NOT NULL,
	protection INT NOT NULL DEFAULT 0,
	PRIMARY KEY(parent, name),
	INDEX file_parentix(parent), 
	FOREIGN KEY (parent) REFERENCES names(id) ON DELETE CASCADE
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS settings
(
	name VARCHAR(100) NOT NULL DEFAULT '',
	string_value VARCHAR(100),
	numeric_value BIGINT,
	PRIMARY KEY(name)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS relationships
(
	id BIGINT NOT NULL AUTO_INCREMENT,
	referer BIGINT NOT NULL DEFAULT 0,
	referee BIGINT NOT NULL DEFAULT 0,
	kind INT NOT NULL DEFAULT 0,
	flags BIGINT NOT NULL DEFAULT 0,
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
