#!/bin/sh
mydate=`date +%Y%m%d`
myfile=backup_$mydate.sql 
echo SET AUTOCOMMIT=0\; > $myfile
echo SET FOREIGN_KEY_CHECKS=0\; >> $myfile
mysqldump -u root --opt --skip-lock-tables --single-transaction kom conferences files magicconferences memberships messageattributes messagelog messagelogpointers messageoccurrences messages names relationships settings userlog users >> $myfile
echo SET FOREIGN_KEY_CHECKS=1\; >> $myfile
echo COMMIT\; >> $myfile
echo SET AUTOCOMMIT=1\; >> $myfile
echo DELETE FROM messagesearch\; >> $myfile
echo INSERT INTO messagesearch SELECT id, subject, body FROM messages\; >> $myfile
echo \; >> $myfile
