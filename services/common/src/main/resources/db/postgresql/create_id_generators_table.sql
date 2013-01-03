/*	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009-2012 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */

/*
 * create_id_generators_table.sql
 *
 * Creates the "id_generators" table, used by the ID Service,
 * and sets the access permissions of that table.
 */

-- Temporarily suppress messages of NOTICE level and below
SET SESSION client_min_messages=WARNING;

-- 'CREATE TABLE ... IF NOT EXISTS' requires PostgreSQL 9.1 or later.
CREATE TABLE IF NOT EXISTS id_generators
(
  csid character varying(80) NOT NULL,
  displayname character varying(80),
  description character varying(500),
  priority integer NOT NULL DEFAULT 9,
  id_generator_state character varying(8000) NOT NULL,
  last_generated_id character varying(255),
  modified timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT id_generators_pkey PRIMARY KEY (csid)
) WITH (
  OIDS=FALSE -- See "Notes" on http://www.postgresql.org/docs/9.1/static/sql-createtable.html
);

CREATE OR REPLACE FUNCTION update_modified_column()
    RETURNS TRIGGER AS
    'BEGIN NEW.modified = now(); RETURN NEW; END;'
    LANGUAGE 'plpgsql';

DROP TRIGGER IF EXISTS update_customer_modtime ON id_generators;
CREATE TRIGGER update_customer_modtime BEFORE UPDATE
    ON id_generators FOR EACH ROW EXECUTE PROCEDURE 
    update_modified_column();

