--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

INSERT INTO `users` (`username`,`passwd`, `tenantid`) VALUES ('test','n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=', '1');

insert into roles (rolename, rolegroup, `tenantid`) values ('collections_manager', 'collections', '1');
insert into roles (rolename, rolegroup, `tenantid`) values ('collections_registrar', 'collections', '1');

insert into users_roles(username, rolename, `tenantid`) values ('test', 'collections_manager', '1');
insert into users_roles(username, rolename, `tenantid`) values('admin', 'collections_registrar', '1');