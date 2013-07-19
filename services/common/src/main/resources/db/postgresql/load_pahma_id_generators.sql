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

/*
 * New id generators for PAHMA (PAHMA-298).
 */

-- LOANS_IN_NUMBER (add a space)

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'cc9d02af-d2e6-47a9-896c-111e83b2c48b',
     'PAHMA Loan In Number',
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
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE 'cc9d02af-d2e6-47a9-896c-111e83b2c48b' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );


-- LOANS_OUT_NUMBER (add a space)

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'a6cc9a5f-de4e-424a-8b73-11878a51392c',
     'PAHMA Loan Out Number',
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
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE 'a6cc9a5f-de4e-424a-8b73-11878a51392c' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- ACCESSION_NUMBER (add "ACC" and a space)

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'eca96f5d-498b-476a-bd4e-ff115c4d2a15',
     'PAHMA Accession Number',
     'Identifies individual collection objects formally
acquired by the institution. Used for collection objects
without parts.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>ACC</initialValue>
      <currentValue>ACC</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE 'eca96f5d-498b-476a-bd4e-ff115c4d2a15' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- OBJECT_EXIT_NUMBER (add a space)

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT
     'a9ba78e7-bf92-4282-bfae-b2ee10c912c4',
     'PAHMA Object Exit Number',
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
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE 'a9ba78e7-bf92-4282-bfae-b2ee10c912c4' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- INTAKE_NUMBER (add a space)

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '83eda2f2-5f29-4d4a-8217-dc1d9f5ce0eb',
     'PAHMA Intake Number',
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
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE '83eda2f2-5f29-4d4a-8217-dc1d9f5ce0eb' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CATALOGING

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '20912112-c22f-4b35-829e-ba33a32980aa',
     'Cataloging Number',
     'PAHMA cataloging number.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>HMA</initialValue>
      <currentValue>HMA</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE '20912112-c22f-4b35-829e-ba33a32980aa' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

-- CLAIM_NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     'c8ca295e-30ce-4903-b8d0-ed0154dee229',
     'PAHMA Claim Number',
     'Identifies a Claim.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>CL</initialValue>
      <currentValue>CL</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <!-- Space character -->
      <initialValue> </initialValue>
      <currentValue> </currentValue>
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
  WHERE 'c8ca295e-30ce-4903-b8d0-ed0154dee229' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );

/*
 * For future reference (see PAHMA-298):
 *
 * And FYI, these are some of the patterns we'll use for some of the not-yet-implemented procedures
 * CR 2012.1.2	 (Condition Checking)
 * INS 2012.1.2	 (Insurance)
 * VAL 2012.1.2	 (Valuation)
 * EXH 2012.1.2	 (Exhibition)
 * USE 2012.1.2	 (Use of Collections)
 * TR 2012.1.2	 (Treatment)
 */