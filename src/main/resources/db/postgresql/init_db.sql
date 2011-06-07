-- drop all the objects before dropping roles
DROP database IF EXISTS jbossdb;
DROP database IF EXISTS cspace;
DROP database IF EXISTS nuxeo;

DROP USER IF EXISTS nuxeo;
DROP USER IF EXISTS cspace;
DROP USER IF EXISTS jboss;

CREATE ROLE nuxeo WITH PASSWORD 'nuxpw' LOGIN;
CREATE ROLE cspace WITH PASSWORD 'cspw' LOGIN;
CREATE ROLE jboss WITH PASSWORD 'jbpw' LOGIN;
CREATE ROLE reader WITH PASSWORD 'read' LOGIN;

--
-- recreate jbossdb, cspace, and nuxeo databases
--
CREATE database jbossdb OWNER jboss;
CREATE DATABASE cspace ENCODING 'UTF8' OWNER cspace;
CREATE DATABASE nuxeo ENCODING 'UTF8' OWNER nuxeo;

--
-- Grant privileges to read-only user on Nuxeo, for reporting. 
--
-- GRANT SELECT ON ALL TABLES IN DATABASE nuxeo TO reader;
-- Will have to do this with a script after the nuxeo DB has been started,
-- since there is no wildcard syntax for grants (!). If nuxeo used a schema, 
-- it would be easy. Check that.
