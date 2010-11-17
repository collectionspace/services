--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

-- Tenants
-- movingimages --
INSERT INTO `cspace`.`tenants` (`id`, `name`, `created_at`) VALUES  ('1','movingimages.us', now());
-- pahma --
--INSERT INTO `cspace`.`tenants` (`id`, `name`, `created_at`) VALUES  ('2','hearstmuseum.berkeley.edu', now());

-- Accounts
-- default test account --
INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('eeca40d7-dc77-4cc5-b489-16a53c75525a','test.test@berkeley.edu',NULL,NULL,'test','ACTIVE','test', now());
-- Additional account introduced during integration on release 0.6, and currently relied upon by the Application Layer.
--INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('251f98f3-0292-4f3e-aa95-455314050e1b','test@collectionspace.org',NULL,NULL,'test@collectionspace.org','ACTIVE','test@collectionspace.org', now());
-- PAHMA test account --
--INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('ff2b4440-ed0d-4892-adb4-b6999eba3ae7','test@hearstmuseum.berkeley.edu',NULL,NULL,'test-pahma','ACTIVE','test-pahma', now());

-- Association of accounts with tenants
INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('eeca40d7-dc77-4cc5-b489-16a53c75525a', '1');
--INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('251f98f3-0292-4f3e-aa95-455314050e1b', '1');
--INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('ff2b4440-ed0d-4892-adb4-b6999eba3ae7', '2');
