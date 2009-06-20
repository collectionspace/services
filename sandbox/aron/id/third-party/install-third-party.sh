#! /bin/sh

mvn install:install-file \
	-Dfile=./commons-id-1.0-SNAPSHOT.jar \
	-DgroupId=org.apache.commons \
  -DartifactId=commons-id \
  -Dversion=1.0-SNAPSHOT \
  -Dpackaging=jar
