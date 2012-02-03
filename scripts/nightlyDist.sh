#!/bin/bash

####################################################
# Script for rolling up a daily tarball from nightly
####################################################

####################################################
# Start of variables to set
####################################################

# Enable for verbose output - uncomment only while debugging!
set -x verbose

# Requires that the $CATALINA_HOME environment variable be set
# to identify the path to the Tomcat folder
ARCHIVE_FOLDER_NAME=`basename "$CATALINA_HOME"`

TARBALL_NAME=$ARCHIVE_FOLDER_NAME-`date +%Y-%m-%d`.tar.gz
DESTINATION_FOLDER=/var/www/html/builds

# The following paths are all relative to the Tomcat folder
NUXEO_CONF_FILE=bin/nuxeo.conf
NUXEO_SERVER_DIR=nuxeo-server
NUXEO_REPO_CONF_FILE=$NUXEO_SERVER_DIR/repos/default/default.xml
NUXEO_DEFAULT_REPO_CONF_FILE=$NUXEO_SERVER_DIR/config/default-repo-config.xml
NUXEO_DATASOURCES_CONF_FILE=$NUXEO_SERVER_DIR/config/datasources-config.xml
WEBAPPS_DIR=webapps
CSPACE_DS_FILE=$WEBAPPS_DIR/cspace-ds.xml
CSPACE_SERVICES_DIR=$WEBAPPS_DIR/cspace-services
WEB_INF_DIR=$CSPACE_SERVICES_DIR/WEB-INF
WEB_INF_CONTEXT_FILE=$WEB_INF_DIR/classes/context.xml
WEB_INF_PERSISTENCE_FILE=$WEB_INF_DIR/classes/META-INF/persistence.xml
META_INF_CONTEXT_FILE=$CSPACE_SERVICES_DIR/META-INF/context.xml
CATALINA_CONF_FILE=conf/Catalina/localhost/cspace-services.xml
TOMCAT_USERS_FILE=conf/tomcat-users.xml
CATALINA_LIB_DIR=lib
APP_LAYER_CONFIG_DIR=$CATALINA_LIB_DIR

####################################################
# End of variables to set
####################################################

echo "Making temporary copy of the Tomcat folder excluding selected items ..."
echo $CATALINA_HOME
rsync -avz \
--exclude 'bin/tomcat.pid' --exclude 'conf/Catalina' --exclude 'cspace' --exclude 'data' \
--exclude 'logs/*' --exclude 'nuxeo-server/*' --exclude 'temp/*' --exclude 'templates' \
--exclude 'webapps/collectionspace' --exclude 'webapps/cspace-ui' --exclude 'webapps/cspace-services' \
--exclude 'webapps/cspace-services.war' --exclude 'work' \
$CATALINA_HOME /tmp

echo "Cleaning up temporary copy of the Tomcat folder ..."
cd /tmp/$ARCHIVE_FOLDER_NAME

echo "Removing passwords from various config files ..."
sed -ri "s/nuxeo\.db\.(user|password)=.*/nuxeo.db.\\1=/" $NUXEO_CONF_FILE
# Note: using sed to edit XML is potentially brittle - ADR
sed -i 's#\(<password>\)[^<].*\(</password>\)#\1\2#g' $CSPACE_DS_FILE
# FIXME: We might look into acting on an array of file paths when
# performing identical replacements, with these three below ...
sed -i 's#\(<property name\=\"[Pp]assword\">\)[^<].*\(</property>\)#\1\2#g' $NUXEO_REPO_CONF_FILE
sed -i 's#\(<property name\=\"[Pp]assword\">\)[^<].*\(</property>\)#\1\2#g' $NUXEO_DEFAULT_REPO_CONF_FILE
sed -i 's#\(<property name\=\"[Pp]assword\">\)[^<].*\(</property>\)#\1\2#g' $NUXEO_DATASOURCES_CONF_FILE
# ... and with the identical replacements within this group as well:
sed -i 's#\(password\=\"\)[^\"]*\(\".*\)#\1\2#g' $WEB_INF_CONTEXT_FILE
sed -i 's#\(password\=\"\)[^\"]*\(\".*\)#\1\2#g' $WEB_INF_PERSISTENCE_FILE
sed -i 's#\(<property name\=\"hibernate.connection.password" value\=\"\)[^"].*\(\"/>\)#\1\2#g' $WEB_INF_PERSISTENCE_FILE
sed -i 's#\(password\=\"\)[^\"]*\(\".*\)#\1\2#g' $META_INF_CONTEXT_FILE
sed -i 's#\(password\=\"\)[^\"]*\(\".*\)#\1\2#g' $CATALINA_CONF_FILE
sed -i 's#\(password\=\"\)[^\"]*\(\".*\)#\1\2#g' $TOMCAT_USERS_FILE
sed -i 's#\(roles\=\"\)[^\"]*\(\".*\)#\1\2#g' $TOMCAT_USERS_FILE
# Note that the above may fail if a double-quote char is part of the password

# This file has already been removed from our prototype Tomcat folder
# echo "Removing jaas.config file ..."
# rm -Rf conf/jaas.config

echo "Removing temporary folders ..."
rm -Rf temp[0-9a-f]*

echo "Creating Nuxeo server plugins folder ..."
mkdir nuxeo-server/plugins

echo "Creating empty Tomcat log file, required by catalina.sh ..."
touch logs/catalina.out

echo "Removing nightly-specific and other host-specific config files ..."
find $APP_LAYER_CONFIG_DIR -name nightly-settings.xml -delete
find $APP_LAYER_CONFIG_DIR -name local-settings.xml -delete

# This command was tested with Fedora Linux 10; other Linux distros and other
# Unix-like operating systems may have slight variations on 'execdir', etc.
echo "Copying settings.xml files to local-settings.xml for each tenant ..."
find $APP_LAYER_CONFIG_DIR/tenants -mindepth 1 -maxdepth 1 -type d \
  -execdir /bin/cp -p '{}'/settings.xml '{}'/local-settings.xml \;

echo "Removing services JAR files ..."
rm -Rf $CATALINA_LIB_DIR/cspace-services-authz.jar
rm -Rf $CATALINA_LIB_DIR/cspace-services-authn.jar

echo "Rolling up tarball ..."
cd /tmp
tar -zcf $TARBALL_NAME $ARCHIVE_FOLDER_NAME

echo "Removing temporary copy of the Tomcat folder ..."
rm -rf /tmp/$ARCHIVE_FOLDER_NAME

echo "Moving tarball to destination folder ..."
mv $TARBALL_NAME $DESTINATION_FOLDER

echo "Deleting all similar tarballs in destination folder older than 7 days ..."
find $DESTINATION_FOLDER -name "$ARCHIVE_FOLDER_NAME-*tar.gz" -mtime +7 -delete

