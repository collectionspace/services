--
-- delete all users
--
-- DELETE FROM mysql.user WHERE NOT (host="localhost" and user="root");
-- FLUSH PRIVILEGES;

--
-- delete anonymous access
--
DELETE FROM mysql.user WHERE User = '';
FLUSH PRIVILEGES;

--
-- recreate jbossdb database
--
DROP database IF EXISTS jbossdb;
CREATE database jbossdb;

--
-- recreate cspace database
--
DROP database IF EXISTS cspace;
CREATE database cspace DEFAULT CHARACTER SET utf8;

--
-- recreate nuxeo database
--
DROP database IF EXISTS nuxeo;
CREATE database nuxeo DEFAULT CHARACTER SET utf8;


--
-- grant privileges to test user on nuxeo and jbossdb databases
--
GRANT ALL PRIVILEGES ON jbossdb.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON cspace.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON nuxeo.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
--
-- Grant privileges to read-only user on Nuxeo, for reporting. 
--
GRANT SELECT ON nuxeo.* TO 'reader'@'localhost' IDENTIFIED BY 'read';
--
-- Grant privileges to remote read-only users on Nuxeo, for reporting. 
-- These should be changed to reflect your domain. Avoid specifying
-- 'reader'@'%' (while simple and flexible, this is a potential security hole).
--
GRANT SELECT ON nuxeo.* TO 'reader'@'%.berkeley.edu' IDENTIFIED BY 'read';
GRANT SELECT ON nuxeo.* TO 'reader'@'%.movingimage.us' IDENTIFIED BY 'read';
FLUSH PRIVILEGES;

