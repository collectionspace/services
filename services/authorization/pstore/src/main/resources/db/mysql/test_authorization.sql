--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

insert into `roles` (`csid`, `rolename`, `displayName`, `rolegroup`, `created_at`, `tenant_id`) values ('-1', 'ROLE_SPRING_ADMIN', 'SPRING_ADMIN', 'Spring Security Administrator', now(), '0');
insert into `roles` (`csid`, `rolename`, `displayName`, `rolegroup`, `created_at`, `tenant_id`) values ('0', 'ROLE_ADMINISTRATOR', 'ADMINISTRATOR', 'CollectionSpace Administrator', now(), '0');

-- for default test account --
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('eeca40d7-dc77-4cc5-b489-16a53c75525a', 'test', '-1', 'ROLE_SPRING_ADMIN', now());
insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('eeca40d7-dc77-4cc5-b489-16a53c75525a', 'test', '0', 'ROLE_ADMINISTRATOR', now());

-- Additional account introduced during integration on release 0.6, and currently relied upon by the Application Layer.
--insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('251f98f3-0292-4f3e-aa95-455314050e1b', 'test@collectionspace.org', '0', 'ROLE_ADMINISTRATOR', now());

-- test account for pahma --
--insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('ff2b4440-ed0d-4892-adb4-b6999eba3ae7', 'test-pahma', '0', 'ROLE_ADMINISTRATOR', now());

-- todo: barney is created in security test but accountrole is not yet created there, so add fake account id
--insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('1', 'barney', '2', 'ROLE_USERS', now());


