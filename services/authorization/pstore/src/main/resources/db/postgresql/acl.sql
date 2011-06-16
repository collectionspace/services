--
-- Copyright 2010 University of California at Berkeley
-- Licensed under the Educational Community License (ECL), Version 2.0.
-- You may not use this file except in compliance with this License.
--

-- use cspace;
DROP TABLE IF EXISTS acl_entry;
DROP TABLE IF EXISTS acl_object_identity;
DROP TABLE IF EXISTS acl_sid;
DROP TABLE IF EXISTS acl_class;

--
-- Table structure for table acl_class
--
CREATE TABLE acl_class(
  id bigserial not null primary key,
  class varchar(100) not null,
  constraint unique_uk_2 unique(class)
);


--
-- Table structure for table acl_sid
--
CREATE TABLE acl_sid(
  id bigserial not null primary key,
  principal boolean not null,
  sid varchar(100) not null,
  constraint unique_uk_1 unique(sid,principal)
);

--
-- Table structure for table acl_object_identity
--
CREATE TABLE acl_object_identity(
  id bigserial primary key,
  object_id_class bigint not null,
  object_id_identity bigint not null,
  parent_object bigint,
  owner_sid bigint,
  entries_inheriting boolean not null,
  constraint unique_uk_3 unique(object_id_class,object_id_identity),
  constraint acl_obj_id_ibfk_1 foreign key(parent_object) references acl_object_identity(id),
  constraint acl_obj_id_ibfk_2 foreign key(object_id_class) references acl_class(id),
  constraint acl_obj_id_ibfk_3 foreign key(owner_sid) references acl_sid(id)
);

--
-- Table structure for table acl_entry
--
CREATE TABLE acl_entry(
  id bigserial primary key,
  acl_object_identity bigint not null,
  ace_order int not null,
  sid bigint not null,
  mask integer not null,
  granting boolean not null,
  audit_success boolean not null,
  audit_failure boolean not null,
  constraint unique_uk_4 unique(acl_object_identity,ace_order),
  constraint acl_entry_ibfk_1 foreign key(acl_object_identity)
      references acl_object_identity(id),
  constraint acl_entry_ibfk_2 foreign key(sid) references acl_sid(id)
);

