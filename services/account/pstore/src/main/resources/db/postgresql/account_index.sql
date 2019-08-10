--
-- Copyright 20010 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--
-- use cspace;
CREATE INDEX IF NOT EXISTS index_userid ON accounts_common (userid);
CREATE INDEX IF NOT EXISTS index_screen_name ON accounts_common (screen_name);
CREATE INDEX IF NOT EXISTS index_email ON accounts_common (email);
CREATE INDEX IF NOT EXISTS index_person_ref_name ON accounts_common (person_ref_name);
CREATE INDEX IF NOT EXISTS index_update_at ON accounts_common (updated_at);
CREATE INDEX IF NOT EXISTS index_status ON accounts_common (status);
