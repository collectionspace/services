--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('ROLE_KERNEL', 'kernel', '2010-02-17 16:31:48');
insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('ROLE_USERS', 'collections', '2010-02-17 16:31:48');
insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('ROLE_COLLECTIONS_MANAGER', 'collections', '2010-02-17 16:31:48');
insert into `roles` (`rolename`, `rolegroup`, `created_at`) values ('ROLE_COLLECTIONS_REGISTRAR', 'collections', '2010-02-17 16:31:48');

insert into `users_roles`(`username`, `rolename`, `created_at`) values ('test', 'ROLE_ADMINISTRATOR', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `rolename`, `created_at`) values ('test', 'ROLE_USERS', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `rolename`, `created_at`) values ('test', 'ROLE_COLLECTIONS_MANAGER', '2010-02-17 16:31:48');

insert into `users_roles`(`username`, `rolename`, `created_at`) values ('barney', 'ROLE_USERS', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `rolename`, `created_at`) values ('barney', 'ROLE_COLLECTIONS_MANAGER', '2010-02-17 16:31:48');
