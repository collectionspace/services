--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

DROP TABLE IF EXISTS users;
CREATE TABLE users(username VARCHAR(128) PRIMARY KEY, passwd VARCHAR(128) NOT NULL );
CREATE INDEX username_users on users(username);

DROP TABLE IF EXISTS roles;
CREATE TABLE roles(rolename VARCHAR(128) PRIMARY KEY, rolegroup VARCHAR(128));
CREATE INDEX rolename_roles on roles(rolename);

DROP TABLE IF EXISTS users_roles;
CREATE TABLE users_roles(username VARCHAR(128) NOT NULL, rolename VARCHAR(128) NOT NULL);
CREATE INDEX username_users_roles on users_roles(username);