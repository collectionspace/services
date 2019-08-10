--
-- Copyright 2010 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--

--
-- Table structure for table acl_class
--
CREATE TABLE IF NOT EXISTS acl_class (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  class VARCHAR(100) NOT NULL UNIQUE
);

--
-- Table structure for table acl_sid
--
CREATE TABLE IF NOT EXISTS acl_sid (
  id BIGSERIAL NOT NULL PRIMARY KEY,
  principal BOOLEAN NOT NULL,
  sid VARCHAR(100) NOT NULL,
  UNIQUE (sid, principal)
);

--
-- Table structure for table acl_object_identity
--
CREATE TABLE IF NOT EXISTS acl_object_identity (
  id BIGSERIAL PRIMARY KEY,
  object_id_class BIGINT NOT NULL,
  object_id_identity BIGINT NOT NULL,
  parent_object BIGINT,
  owner_sid BIGINT,
  entries_inheriting BOOLEAN NOT NULL,
  UNIQUE (object_id_class, object_id_identity),
  FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
  FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
  FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
);

--
-- Table structure for table acl_entry
--
CREATE TABLE IF NOT EXISTS acl_entry (
  id BIGSERIAL PRIMARY KEY,
  acl_object_identity BIGINT NOT NULL,
  ace_order INT NOT NULL,
  sid BIGINT NOT NULL,
  mask INTEGER NOT NULL,
  granting BOOLEAN NOT NULL,
  audit_success BOOLEAN NOT NULL,
  audit_failure BOOLEAN NOT NULL,
  UNIQUE(acl_object_identity,ace_order),
  FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
  FOREIGN KEY (sid) REFERENCES acl_sid (id)
);
