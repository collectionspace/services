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
-- recreate cspace database
--
DROP database IF EXISTS cspace;
CREATE database cspace DEFAULT CHARACTER SET utf8;

--
-- grant privileges to users on cspace database
--
GRANT ALL PRIVILEGES ON cspace.* TO '@DB_CSPACE_USER@'@'localhost' IDENTIFIED BY '@DB_CSPACE_PASSWORD@' WITH GRANT OPTION;
FLUSH PRIVILEGES;

