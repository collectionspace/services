-- drop all the objects before dropping roles
DROP database IF EXISTS cspace;

DROP USER IF EXISTS cspace;

CREATE ROLE @DB_CSPACE_USER@ WITH PASSWORD '@DB_CSPACE_PASSWORD@' LOGIN;

--
-- recreate cspace database
--
CREATE DATABASE cspace ENCODING 'UTF8' OWNER @DB_CSPACE_USER@;
