/*	
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
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
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */

DROP TABLE IF EXISTS `id_generators`;
CREATE TABLE `id_generators` (
  `csid`                varchar(80) PRIMARY KEY,
  `displayname`         varchar(80),
  `description`         varchar(500),
  `priority`            smallint(1) DEFAULT 9 NOT NULL,
  `id_generator_state`  varchar(8000) NOT NULL,
  `last_generated_id`   varchar(255),
  `modified`            timestamp NOT NULL
                        default CURRENT_TIMESTAMP
                        on update CURRENT_TIMESTAMP,
  INDEX `csid_index` (`csid`)
) ENGINE=InnoDB;


SHOW WARNINGS;
