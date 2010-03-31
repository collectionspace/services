--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`) values ('1', 'ROLE_ADMINISTRATOR', 'admin', '2010-02-17 16:31:48');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`) values ('2', 'ROLE_USERS', 'collections', '2010-02-17 16:31:48');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`) values ('3', 'ROLE_COLLECTIONS_MANAGER', 'collections', '2010-02-17 16:31:48');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`) values ('4', 'ROLE_COLLECTIONS_REGISTRAR', 'collections', '2010-02-17 16:31:48');

insert into `users_roles`(`username`, `role_id`, `created_at`) values ('test', '1', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `role_id`, `created_at`) values ('test', '2', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `role_id`, `created_at`) values ('test', '3', '2010-02-17 16:31:48');

insert into `users_roles`(`username`, `role_id`, `created_at`) values ('barney', '2', '2010-02-17 16:31:48');
insert into `users_roles`(`username`, `role_id`, `created_at`) values ('barney', '3', '2010-02-17 16:31:48');
