@echo off
java -classpath conf;lib\kom.jar;distr\log4j-1.2.8.jar;distr\mysql-connector-java-3.0.16-ga-bin.jar;distr\concurrent.jar;distr\xercesImpl.jar;distr\xml-apis.jar;distr\commons-logging.jar;distr\j2ssh-common-0.2.7.jar;distr\j2ssh-core-0.2.7.jar;distr\j2ssh-daemon-0.2.7.jar;distr\j2ssh-ext.1.1.0.jar nu.rydin.kom.sklaff.SklaffConvert %1 %2 %3
