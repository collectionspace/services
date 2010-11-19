--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

-- Tenants
-- default cspace --
-- ONLY Needed for the hack below.
-- INSERT INTO `cspace`.`tenants` (`id`, `name`, `created_at`) VALUES  ('1','collectionspace.org', now());

-- Accounts
-- default bootstrap user required to run ImportAuthZ (to bootstrap Spring) --
-- INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('Bootstrapping-Account-DO-NOT-DELETE','bootstrap@collectionspace.org',NULL,NULL,'SPRING_ADMIN','ACTIVE','SPRING_ADMIN', now());

-- Association of accounts with tenants
-- INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('Bootstrapping-Account-DO-NOT-DELETE', '1');
