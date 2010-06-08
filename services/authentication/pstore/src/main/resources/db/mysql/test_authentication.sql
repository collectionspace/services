--
-- Copyright 2009 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
use cspace;

-- default test user --
insert into `users` (`username`,`passwd`, `created_at`) VALUES ('test','n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=', now());
-- Additional account introduced during integration on release 0.6, and currently relied upon by the Application Layer.
insert into `users` (`username`,`passwd`, `created_at`) VALUES ('test@collectionspace.org','NyaDNd1pMQRb3N+SYj/4GaZCRLU9DnRtQ4eXNJ1NpXg=', now());
-- user for testing pahma deployment --
insert into `users` (`username`,`passwd`, `created_at`) VALUES ('test-pahma','n4bQgYhMfWWaL+qgxVrQFaO/TxsrC4Is0V1sFbDwCgg=', now());
