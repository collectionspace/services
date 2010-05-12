--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`, `tenant_id`) values ('1', 'ROLE_ADMINISTRATOR', 'admin', '2010-02-17 16:31:48', '0');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`, `tenant_id`) values ('2', 'ROLE_USERS', 'collections', '2010-02-17 16:31:48', '1');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`, `tenant_id`) values ('3', 'ROLE_COLLECTIONS_MANAGER', 'collections', '2010-02-17 16:31:48', '1');
insert into `roles` (`csid`, `rolename`, `rolegroup`, `created_at`, `tenant_id`) values ('4', 'ROLE_COLLECTIONS_REGISTRAR', 'collections', '2010-02-17 16:31:48', '1');

insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('eeca40d7-dc77-4cc5-b489-16a53c75525a', 'test', '1', 'ROLE_ADMINISTRATOR', '2010-02-17 16:31:48');
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('eeca40d7-dc77-4cc5-b489-16a53c75525a', 'test', '2', 'ROLE_USERS', '2010-02-17 16:31:48');
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('eeca40d7-dc77-4cc5-b489-16a53c75525a', 'test', '3', 'ROLE_COLLECTIONS_MANAGER', '2010-02-17 16:31:48');

-- Additional account introduced during integration on release 0.6, and currently relied upon by the Application Layer.
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('251f98f3-0292-4f3e-aa95-455314050e1b', 'test@collectionspace.org', '1', 'ROLE_ADMINISTRATOR', '2010-05-03 12:35:00');
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('251f98f3-0292-4f3e-aa95-455314050e1b', 'test@collectionspace.org', '2', 'ROLE_USERS', '2010-05-03 12:35:00');
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('251f98f3-0292-4f3e-aa95-455314050e1b', 'test@collectionspace.org', '3', 'ROLE_COLLECTIONS_MANAGER', '2010-05-03 12:35:00');

-- todo: barney is created in security test but accountrole is not yet created there, so add fake account id
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('1', 'barney', '2', 'ROLE_USERS', '2010-02-17 16:31:48');
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('1', 'barney', '3', 'ROLE_COLLECTIONS_MANAGER', '2010-02-17 16:31:48');
