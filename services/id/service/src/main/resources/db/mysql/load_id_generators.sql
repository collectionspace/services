/*	
 * load_id_generators_table.sql
 *
 * Loads an initial set of ID patterns into the "id_generators" table.
 *
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
 *
 * $LastChangedBy: aron $
 * $LastChangedRevision: 302 $
 * $LastChangedDate$
 */

USE `cspace`;

-- ACCESSION_LOT_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('ACCESSION_LOT_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>ACCESSION_LOT_NUMBER</csid>
  <uri></uri>
  <description>Generates accession numbers, identifying accession events in which a lot of one or more collection objects are acquired by the museum.</description>
  <parts>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- ACCESSION_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('ACCESSION_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>ACCESSION_NUMBER</csid>
  <uri></uri>
  <description>Generates accession numbers, to identify individual collection objects individually acquired by the museum.  This generator is used items without parts.</description>
  <parts>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- ARCHIVES_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('ARCHIVES_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>ARCHIVES_NUMBER</csid>
  <uri></uri>
  <description>Generates archives numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>AR</initialValue>
      <currentValue>AR</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- EVALUATION_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('EVALUATION_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>EVALUATION_NUMBER</csid>
  <uri></uri>
  <description>Generates evaluation numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>EV</initialValue>
      <currentValue>EV</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- INTAKE_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('INTAKE_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>INTAKE_NUMBER</csid>
  <uri></uri>
  <description>Generates intake numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IN</initialValue>
      <currentValue>IN</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- INTAKE_OBJECT_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('INTAKE_OBJECT_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>INTAKE_OBJECT_NUMBER</csid>
  <uri></uri>
  <description>Generates intake numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>IN</initialValue>
      <currentValue>IN</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- LIBRARY_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('LIBRARY_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>LIBRARY_NUMBER</csid>
  <uri></uri>
  <description>Generates library numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LIB</initialValue>
      <currentValue>LIB</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- LOANS_IN_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('LOANS_IN_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>LOANS_IN_NUMBER</csid>
  <uri></uri>
  <description>Generates loans-in numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>LI</initialValue>
      <currentValue>LI</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

-- STUDY_NUMBER

INSERT INTO `id_generators`
    (id_generator_csid, last_generated_id, id_generator_state)
  VALUES 
    ('STUDY_NUMBER', 
     '',
'<org.collectionspace.services.id.BaseIDGenerator>
  <csid>STUDY_NUMBER</csid>
  <uri></uri>
  <description>Generates study numbers.</description>
  <parts>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>ST</initialValue>
      <currentValue>ST</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.YearIDGeneratorPart>
      <initialValue>2009</initialValue>
      <currentValue>2009</currentValue>
    </org.collectionspace.services.id.YearIDGeneratorPart>
    <org.collectionspace.services.id.StringIDGeneratorPart>
      <initialValue>.</initialValue>
      <currentValue>.</currentValue>
    </org.collectionspace.services.id.StringIDGeneratorPart>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>0</initialValue>
      <currentValue>0</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.BaseIDGenerator>');

SHOW WARNINGS;
