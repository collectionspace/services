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
 * load_botgarden_id_generators.sql
 *
 * Loads botgarden-specific data into the "id_generators" table,
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


-- UC_ACCESSION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'ed3c8578-cf01-4a05-b7fc-8e49b96273c6',
     'UC Accession Number',
     'Identifies individual collection objects formally
acquired by the UC Herbarium. Used for collection objects
without parts.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>UC</initialValue>
      <currentValue>UC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>9</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
WHERE 'ed3c8578-cf01-4a05-b7fc-8e49b96273c6' NOT IN
      (
      SELECT  csid
      FROM    id_generators
      );

-- JEPS_ACCESSION_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'd9eca382-23b7-47aa-8d9b-d7ff88f49e2b',
     'JEPS Accession Number',
     'Identifies individual collection objects formally
acquired by the Jepson Herbarium. Used for collection objects
without parts.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>JEPS</initialValue>
      <currentValue>JEPS</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>9</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
WHERE 'd9eca382-23b7-47aa-8d9b-d7ff88f49e2b' NOT IN
      (
      SELECT  csid
      FROM    id_generators
      );

-- VOUCHER_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '940710aa-3df4-4bd4-8d6f-9bf6a92b35cc',
     'Voucher Collection Number',
     'Identifies Botanical Garden vouchers.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>V</initialValue>
      <currentValue>V</currentValue>
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
WHERE '940710aa-3df4-4bd4-8d6f-9bf6a92b35cc' NOT IN
      (
      SELECT  csid
      FROM    id_generators
      );

-- PROP_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '81cf5a56-d43d-49e9-ac11-61cf4b0923d4',
     'Propagation Number',
     'Identifies activities in which accessions are
propagated.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>9</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
WHERE '81cf5a56-d43d-49e9-ac11-61cf4b0923d4' NOT IN
      (
      SELECT  csid
      FROM    id_generators
      );

-- POTTAG

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     '3e2e4115-157e-4598-a34b-44224f23c721',
     'Pot Tag Number',
     'Reference number for a Pot Tag.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>PT</initialValue>
      <currentValue>PT</currentValue>
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
  WHERE '3e2e4115-157e-4598-a34b-44224f23c721' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );
