--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into `users` (`username`,`passwd`, `created_at`) VALUES ('test','n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=', '2010-02-17 16:31:48');

insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('kernel', 'kernel', '2010-02-17 16:31:48');
insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('collections_manager', 'collections', '2010-02-17 16:31:48');
insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('collections_registrar', 'collections', '2010-02-17 16:31:48');

insert into `users_roles`(`username`, `rolename`, `created_at`) values ('test', 'collections_manager', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `rolename`, `created_at`) values('admin', 'collections_registrar', '2010-02-17 16:31:48');