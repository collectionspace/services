-- drop all the objects before dropping roles
DROP database IF EXISTS nuxeo;

DROP USER IF EXISTS nuxeo;
DROP USER IF EXISTS reader;

CREATE ROLE @DB_NUXEO_USER@ WITH PASSWORD '@DB_NUXEO_PASSWORD@' LOGIN;
CREATE ROLE reader WITH PASSWORD 'read' LOGIN;

--
-- recreate nuxeo database
--
CREATE DATABASE nuxeo ENCODING 'UTF8' OWNER @DB_NUXEO_USER@;

--
-- Grant privileges to read-only user on Nuxeo, for reporting. 
--
GRANT CONNECT ON DATABASE nuxeo TO reader;

-- GRANT SELECT ON ALL TABLES IN SCHEMA public TO reader;
-- This must be run by hand, after the system has already started up,
-- so that it gives access to all the tables created on init.

