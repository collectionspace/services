-- init_nuxeo_db.sql

-- This file holds SQL statements which can be run to initialize
-- the Nuxeo-managed databases. A copy of this file is deployed to the 
-- CollectionSpace server folder during a build of the Services layer.

-- Beginning with CollectionSpace version 4.1, the contents of the deployed
-- copy of this file are no longer static. Rather, that file's contents are
-- now written during each CollectionSpace system startup, via Services layer code.

-- You can manually invoke the initialization commands in that file
-- by entering the command "ant create_nuxeo_db" from the top of the
-- Services layer source code tree. (Please note that doing so will DELETE
-- the Nuxeo-managed databases and their associated user roles, and
-- will irrevocably DESTROY your data contained in those databases.)

-- Any additional initialization statements not already written to that
-- file during startup can be added here, following this comment. You'll
-- need to deploy this file to the CollectionSpace server folder, to have
-- those additional statements take effect:
