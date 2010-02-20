--
-- Copyright 20010 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;
CREATE INDEX index_rolename ON roles (rolegroup);
CREATE INDEX index_username ON users_roles (username);
CREATE INDEX index_rolename ON users_roles (rolename);
