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
 * load_id_generators_table.sql
 *
 * Loads a default set of data into the "id_generators" table,
 * used by the ID Service.
 *
 * $LastChangedRevision$
 * $LastChangedDate$
 */

/*
 * Note: in the priority column, values range from  '1' (highest)
 * to '9' (lowest).
 */

/*
 * NOTE: In the id_generator_state column, for numeric sequence parts
 * whose first generated value should start at the initial value
 * (such as '1'), enter '-1' for the <currentValue>
 *
 * Otherwise, the first generated value will be the next value
 * in the sequence after the initial value (e.g. '2', if the
 * initial value is '1').
 */

-- EXHIBITION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '29ff8c5e-597a-41c6-a481-6e92dfe0a59f',
     'Exhibition Number',
     'Identifies exhibition activities, in which one
or more exhibition events occur at an institution.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
   <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>EXH</initialValue>
      <currentValue>EXH</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '29ff8c5e-597a-41c6-a481-6e92dfe0a59f' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );
        
-- ACCESSION_LOT_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '1a67470b-19b1-4ae3-88d4-2a0aa936270e',
     'Accession Activity Number',
     'Identifies accession activities, in which a lot of
one or more collection objects is acquired by the institution.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '1a67470b-19b1-4ae3-88d4-2a0aa936270e' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- ACCESSION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '9dd92952-c384-44dc-a736-95e435c1759c',
     'Accession Number',
     'Identifies individual collection objects formally
acquired by the institution. Used for collection objects
without parts.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '9dd92952-c384-44dc-a736-95e435c1759c' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- ARCHIVES_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '70586d30-9dca-4a07-a3a2-1976fe898028',
     'Archives Number',
     'Identifies archives-related accession activities,
in which a lot of one or more collection objects is formally
acquired for the archives.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>AR</initialValue>
      <currentValue>AR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '70586d30-9dca-4a07-a3a2-1976fe898028' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CLAIM_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'a253d167-4f1a-4be3-a477-a2bd8a30cd7f',
     'Claim Number',
     'Identifies a Claim.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>CL</initialValue>
      <currentValue>CL</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'a253d167-4f1a-4be3-a477-a2bd8a30cd7f' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CONDITIONCHECK_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '585af100-1a35-11e2-892e-0800200c9a66',
     'Condition Check Number',
     'Identifies a condition check record.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>CC</initialValue>
      <currentValue>CC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '585af100-1a35-11e2-892e-0800200c9a66' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- EVALUATION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'd2d80822-25c7-4c7c-a105-fc40cdb0c50f',
     'Evaluation Number',
     'Identifies evaluation-related intake activities,
in which a lot of one or more collection objects is formally
acquired for evaluation.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>EV</initialValue>
      <currentValue>EV</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'd2d80822-25c7-4c7c-a105-fc40cdb0c50f' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- UOC_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '9088cfa5-d743-5824-cb4d-eb11b12847f7',
     'Use of Collections Number',
     'Identifies Use of Collections activities.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
   <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>UOC</initialValue>
      <currentValue>UOC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '9088cfa5-d743-5824-cb4d-eb11b12847f7' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );
        
-- INTAKE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '8088cfa5-c743-4824-bb4d-fb11b12847f7',
     'Intake Number',
     'Identifies intake activities, in which a lot of one
or more collection objects enters the institution.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
   <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IN</initialValue>
      <currentValue>IN</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '8088cfa5-c743-4824-bb4d-fb11b12847f7' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- INTAKE_OBJECT_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'a91db555-5c53-4996-9918-6712351397a0',
     'Intake Object Number',
     'Identifies individual collection objects that enter
the institution through intake activities, before they are 
either returned to their owner or formally acquired.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IN</initialValue>
      <currentValue>IN</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'a91db555-5c53-4996-9918-6712351397a0' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- INVENTORY_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '6d472be6-2534-47f3-a3f1-3f160e7a9303',
     'Inventory Number',
     'Unambiguously identifies a location associated with an inventory event.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>INV</initialValue>
      <currentValue>INV</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '6d472be6-2534-47f3-a3f1-3f160e7a9303' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- LIBRARY_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '80fedaf6-1647-4f30-9f53-a75a3cac2ffd',
     'Library Number',
     'Identifies library-related accession activities,
in which a lot of one or more collection objects is
formally acquired for the library.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
   <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LIB</initialValue>
      <currentValue>LIB</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '80fedaf6-1647-4f30-9f53-a75a3cac2ffd' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- LOANS_IN_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'ed87e7c6-0678-4f42-9d33-f671835586ef',
     'Loan In Number',
     'Identifies activities in which collection objects are
received on loan.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LI</initialValue>
      <currentValue>LI</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'ed87e7c6-0678-4f42-9d33-f671835586ef' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- LOANS_OUT_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '4b984865-f93d-4481-b874-3dba863ec589',
     'Loan Out Number',
     'Identifies activities in which collection objects are
loaned out of the institution.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LO</initialValue>
      <currentValue>LO</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '4b984865-f93d-4481-b874-3dba863ec589' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- LOCATION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '1fc5e383-0786-4126-9a3c-ec7df4517ee3',
     'Location Number',
     'Unambiguously identifies a general location, not associated with an
inventory or movement event.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LOC</initialValue>
      <currentValue>LOC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '1fc5e383-0786-4126-9a3c-ec7df4517ee3' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- MEDIA_RESOURCE_IDENTIFICATION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'cd91d8b8-f346-4925-a425-93e02bd1c5c9',
     'Media Resource Identification Number',
     'Unambiguously identifies a media resource within a given context.
Recommended best practice is to identify the resource by means of a string
conforming to a formal identification system.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>MR</initialValue>
      <currentValue>MR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'cd91d8b8-f346-4925-a425-93e02bd1c5c9' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- MOVEMENT_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '49ca9d8d-7136-47ff-a70e-4a47b9038b70',
     'Movement Reference Number',
     'Identifies a movement of a collection object or a group of collection objects.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>MV</initialValue>
      <currentValue>MV</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '49ca9d8d-7136-47ff-a70e-4a47b9038b70' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- OBJECT_EXIT_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'd4eea707-d473-4367-853a-728fbcd9be17',
     'Object Exit Number',
     'Identifies an exit from the museum of collection objects, via
transfer or destruction, and the deaccessioning of those objects
from the museum''s collections.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>EX</initialValue>
      <currentValue>EX</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'd4eea707-d473-4367-853a-728fbcd9be17' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- STUDY_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '0518132e-dd8c-4773-8fa9-07c9af4444ee',
     'Study Number',
     'Identifies study-related intake activities,
in which a lot of one or more collection objects is
formally acquired for study.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
   <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>ST</initialValue>
      <currentValue>ST</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '0518132e-dd8c-4773-8fa9-07c9af4444ee' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- TRANSFER_OF_TITLE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'c597a209-5954-4fa6-bf3f-f83c1a0ad586',
     'Transfer of Title Number',
     'Identifies the transfer of title for one or more collection objects
to a receiving institution.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>TT</initialValue>
      <currentValue>TT</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'c597a209-5954-4fa6-bf3f-f83c1a0ad586' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- UUID

/*
 * Note: these are Type 4 UUIDs, whose generation is based on
 * random and pseudo-random parts.
 */

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '1fa40353-05b8-4ae6-82a6-44a18b4f3c12',
     'UUID',
     'Universally unique identifiers (UUIDs), which may be
used for CollectionSpace IDs (CSIDs) and any other relevant
purposes.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.UUIDGeneratorPart>
    </org.collectionspace.services.id.UUIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '1fa40353-05b8-4ae6-82a6-44a18b4f3c12' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- VALUATION_CONTROL_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'eafbc0cd-70fe-4802-8476-b931b1b0e381',
     'Valuation Control Reference Number',
     'Identifies the valuation control of an object record.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>VC</initialValue>
      <currentValue>VC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'eafbc0cd-70fe-4802-8476-b931b1b0e381' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CONSERVATION_TREATMENT_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'aad54202-404d-4f19-ada9-8b1e378ad1b2',
     'Conservation Treatment Reference Number',
     'Identifies a conservation treatment.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>CT</initialValue>
      <currentValue>CT</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'aad54202-404d-4f19-ada9-8b1e378ad1b2' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- TRANSPORT_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
    SELECT
        'cc92bbc1-014a-4673-a81d-0c14375375d0',
        'Transport Reference Number',
        'Identifies a Transport.',
        '9',
        '',
   '<org.collectionspace.services.id.SettableIDGenerator>
     <parts>
       <org.collectionspace.services.id.StringIDGeneratorPart>
         <initialValue>TR</initialValue>
         <currentValue>TR</currentValue>
       </org.collectionspace.services.id.StringIDGeneratorPart>
       <org.collectionspace.services.id.YearIDGeneratorPart>
         <currentValue></currentValue>
       </org.collectionspace.services.id.YearIDGeneratorPart>
       <org.collectionspace.services.id.StringIDGeneratorPart>
         <initialValue>.</initialValue>
         <currentValue>.</currentValue>
       </org.collectionspace.services.id.StringIDGeneratorPart>
       <org.collectionspace.services.id.NumericIDGeneratorPart>
         <maxLength>6</maxLength>
         <initialValue>1</initialValue>
         <currentValue>-1</currentValue>
       </org.collectionspace.services.id.NumericIDGeneratorPart>
     </parts>
   </org.collectionspace.services.id.SettableIDGenerator>'
    WHERE 'cc92bbc1-014a-4673-a81d-0c14375375d0' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );
-- INSURANCE_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'e6f52346-0d2c-4b68-8a35-0a27dad2f3f4',
     'Insurance Reference Number',
     'Identifies an insurance.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>INS</initialValue>
      <currentValue>INS</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE 'e6f52346-0d2c-4b68-8a35-0a27dad2f3f4' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- INDEMNITY_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '26583488-7cb4-42b5-a8e2-4db005c8f5ef',
     'Indemnity Reference Number',
     'Identifies an indemnity.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IND</initialValue>
      <currentValue>IND</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '26583488-7cb4-42b5-a8e2-4db005c8f5ef' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- ITERATIONREPORT_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '4f0f36f6-643c-4a3e-89f4-012837852b04',
     'Iteration Report Reference Number',
     'Identifies an iterationreport.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IR</initialValue>
      <currentValue>IR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '4f0f36f6-643c-4a3e-89f4-012837852b04' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- DUTYOFCARE_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '6205cfe5-d144-4e89-b641-0e8e4b356f34',
     'Duty of Care Reference Number',
     'Identifies a dutyofcare.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>DC</initialValue>
      <currentValue>DC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '6205cfe5-d144-4e89-b641-0e8e4b356f34' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- NAGPRAINVENTORY_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '500b27f0-2ad9-42ab-af0b-3452a4ad5d29',
     'NAGPRA Inventory Report Reference Number',
     'Identifies a nagprainventory.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>INV</initialValue>
      <currentValue>INV</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '500b27f0-2ad9-42ab-af0b-3452a4ad5d29' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- SUMMARY_DOCUMENTATION_REFERENCE_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '54ab6096-11e0-44a3-9a41-06a87c1c1bdb',
     'Summary Documentation Reference Number',
     'Identifies a summary documentation.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>SD</initialValue>
      <currentValue>SD</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '54ab6096-11e0-44a3-9a41-06a87c1c1bdb' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- HELD_IN_TRUST_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '6b5bc008-34f9-40be-bd49-0440ee9f262d',
     'Held-in-Trust Number',
     'Identifies a held-in-trust document.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>HIT</initialValue>
      <currentValue>HIT</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '6b5bc008-34f9-40be-bd49-0440ee9f262d' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CONSULTATION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '48dfa8e6-6c18-4d6e-bb2d-bbc69cc2cb36',
     'Consultation Number',
     'Identifies a consultation document.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>CN</initialValue>
      <currentValue>CN</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '48dfa8e6-6c18-4d6e-bb2d-bbc69cc2cb36' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- DEACCESSION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '0b508d05-67df-4b63-b4ce-7319a3029baa',
     'Deaccession Number',
     'Identifies a deaccession document.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>DE</initialValue>
      <currentValue>DE</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '0b508d05-67df-4b63-b4ce-7319a3029baa' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- REPATRIATION_REQUEST_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '7f6e2d67-b95a-4a0c-841c-5a00a9cd1db0',
     'Repatriation Request Number',
     'Identifies a repatriation request document.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>RR</initialValue>
      <currentValue>RR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '7f6e2d67-b95a-4a0c-841c-5a00a9cd1db0' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- RESTRICTED_MEDIA_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '17772cbd-8de1-4969-ac2e-c51798bc4e75',
     'Restricted Media Number',
     'Identifies a restricted media document.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>RMR</initialValue>
      <currentValue>RMR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <currentValue></currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '17772cbd-8de1-4969-ac2e-c51798bc4e75' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );
