--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;
INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('eeca40d7-dc77-4cc5-b489-16a53c75525a','test.test@berkeley.edu',NULL,NULL,'test','ACTIVE','test', '2010-02-17 16:31:48');
INSERT INTO `cspace`.`tenants` (`id`, `name`, `created_at`) VALUES  ('1','movingimages.us', '2010-02-17 16:31:48');
INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('eeca40d7-dc77-4cc5-b489-16a53c75525a', '1');

-- Added default login / end user account for release 0.6
INSERT INTO `cspace`.`accounts_common` (`csid`, `email`, `phone`, `mobile`, `userid`, `status`, `screen_name`, `created_at`) VALUES  ('251f98f3-0292-4f3e-aa95-455314050e1b','test@collectionspace.org',NULL,NULL,'test@collectionspace.org','ACTIVE','test@collectionspace.org', '2010-05-03 12:35:00');
INSERT INTO `cspace`.`accounts_tenants` (`TENANTS_ACCOUNTSCOMMON_CSID`, `tenant_id`) VALUES ('251f98f3-0292-4f3e-aa95-455314050e1b', '1');
