--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

-- Tenants
-- default cspace --
-- ONLY Needed for the hack below.
INSERT INTO `cspace`.`tenants` (`id`, `name`, `created_at`) VALUES  ('1','collectionspace.org', now());

-- Accounts
-- default test account --
INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('eeca40d7-dc77-4cc5-b489-16a53c75525a','test.test@berkeley.edu',NULL,NULL,'test','ACTIVE','test', now());

-- Association of accounts with tenants
INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('eeca40d7-dc77-4cc5-b489-16a53c75525a', '1');
