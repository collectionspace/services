--
-- Copyright 20010 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;
CREATE INDEX index_rolename ON roles (rolename);
CREATE INDEX index_rolegroup ON roles (rolegroup);
CREATE INDEX index_tenant_id ON roles (tenant_id);
CREATE INDEX index_username ON users_roles (username);
CREATE INDEX index_role_id ON users_roles (role_id);
CREATE INDEX index_permission_id ON permissions_roles (permission_id);
CREATE INDEX index_role_id ON permissions_roles (role_id);
