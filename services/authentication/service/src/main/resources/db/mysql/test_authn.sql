--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into users (username, passwd) values ('test', 'test');
insert into users (username, passwd) values ('admin', 'admin');

insert into roles (rolename, rolegroup) values ('collections_manager', 'collections');
insert into roles (rolename, rolegroup) values ('collections_registrar', 'collections');

insert into users_roles(username, rolename) values ('test', 'collections_manager');
insert into users_roles(username, rolename) values('admin', 'collections_registrar');