--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

INSERT INTO `users` (`username`,`passwd`) VALUES ('test','n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=');

insert into roles (rolename, rolegroup) values ('collections_manager', 'collections');
insert into roles (rolename, rolegroup) values ('collections_registrar', 'collections');

insert into users_roles(username, rolename) values ('test', 'collections_manager');
insert into users_roles(username, rolename) values('admin', 'collections_registrar');