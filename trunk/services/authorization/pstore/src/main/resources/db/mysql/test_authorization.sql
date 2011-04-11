--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

-- insert into `roles` (`csid`, `rolename`, `displayName`, `rolegroup`, `created_at`, `tenant_id`) values ('-1', 'ROLE_SPRING_ADMIN', 'SPRING_ADMIN', 'Spring Security Administrator', now(), '0');
-- insert into `roles` (`csid`, `rolename`, `displayName`, `rolegroup`, `created_at`, `tenant_id`) values ('0', 'ROLE_ADMINISTRATOR', 'ADMINISTRATOR', 'CollectionSpace Administrator', now(), '0');

-- for default bootstrap user required to run ImportAuthZ (to bootstrap Spring) --
-- insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('Bootstrapping-Account-DO-NOT-DELETE', 'test', '-1', 'ROLE_SPRING_ADMIN', now());
-- insert into `accounts_roles`(`account_id`, `user_id`, `role_id`, `role_name`, `created_at`) values ('Bootstrapping-Account-DO-NOT-DELETE', 'test', '0', 'ROLE_ADMINISTRATOR', now());

