--
-- Create and initialize CollectionSpace service database environment for MySQL
--

--
-- delete all users
--
DELETE FROM mysql.user WHERE NOT (host="localhost" and user="root");
FLUSH PRIVILEGES;

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
CREATE database cspace;

--
-- recreate nuxeo database
--
DROP database IF EXISTS nuxeo;
CREATE database nuxeo;


--
-- grant privileges to test user on nuxeo and jbossdb databases
--
GRANT ALL PRIVILEGES ON jbossdb.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON cspace.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
FLUSH PRIVILEGES;
GRANT ALL PRIVILEGES ON nuxeo.* TO 'test'@'localhost' IDENTIFIED BY 'test' WITH GRANT OPTION;
FLUSH PRIVILEGES;

