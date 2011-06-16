-- drop all the objects before dropping roles
DROP database IF EXISTS jbossdb;
DROP database IF EXISTS cspace;
DROP database IF EXISTS nuxeo;

DROP USER IF EXISTS nuxeo;
DROP USER IF EXISTS cspace;
DROP USER IF EXISTS jboss;
DROP USER IF EXISTS reader;

CREATE ROLE @DB_NUXEO_USER@ WITH PASSWORD '@DB_NUXEO_PASSWORD@' LOGIN;
CREATE ROLE @DB_CSPACE_USER@ WITH PASSWORD '@DB_CSPACE_PASSWORD@' LOGIN;
CREATE ROLE @DB_JBOSS_USER@ WITH PASSWORD '@DB_JBOSS_PASSWORD@' LOGIN;
CREATE ROLE reader WITH PASSWORD 'read' LOGIN;

--
-- recreate jbossdb, cspace, and nuxeo databases
--
CREATE database jbossdb OWNER @DB_JBOSS_USER@;
CREATE DATABASE cspace ENCODING 'UTF8' OWNER @DB_CSPACE_USER@;
CREATE DATABASE nuxeo ENCODING 'UTF8' OWNER @DB_NUXEO_USER@;

--
-- Grant privileges to read-only user on Nuxeo, for reporting. 
--
-- GRANT SELECT ON ALL TABLES IN DATABASE nuxeo TO reader;
-- Will have to do this with a script after the nuxeo DB has been started,
-- since there is no wildcard syntax for grants (!). If nuxeo used a schema, 
-- it would be easy. Check that.
