-- db1: {herbarium45_default localhost 5432 csadmin cs1dn3b sslmode=disable}
-- db2: {herbarium_default localhost 5432 csadmin cs1dn3b sslmode=disable}
-- Run the following SQL againt db2:
DROP TABLE IF EXISTS collectionobjects_naturalhistory;

-- Rename these tables and corresponding columns

ALTER TABLE associatedtaxagroup DROP CONSTRAINT IF EXISTS associatedtaxagroup_pk CASCADE;
ALTER TABLE associatedtaxagroup DROP CONSTRAINT IF EXISTS associatedtaxagroup_id_hierarchy_fk CASCADE;
ALTER TABLE associatedtaxagroup RENAME TO herbassociatedtaxagroup;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxon TO herbAssocTaxon;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxonCommonName TO herbAssocTaxonCommonName;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxonInteraction TO herbAssocTaxonInteraction;
ALTER TABLE herbassociatedtaxagroup ADD CONSTRAINT herbassociatedtaxagroup_pk PRIMARY KEY (id);
ALTER TABLE herbassociatedtaxagroup ADD CONSTRAINT herbassociatedtaxagroup_id_hierarchy_fk FOREIGN KEY (id) REFERENCES hierarchy (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE localitygroup DROP CONSTRAINT IF EXISTS localitygroup_pk CASCADE;
ALTER TABLE localitygroup DROP CONSTRAINT IF EXISTS localitygroup_id_hierarchy_fk CASCADE;
ALTER TABLE localitygroup RENAME TO herblocalitygroup;
ALTER TABLE herblocalitygroup RENAME COLUMN collectionLocationVerbatim TO herbCollectionLocationVerbatim;
ALTER TABLE herblocalitygroup RENAME COLUMN collectionPlace TO herbCollectionPlace;
ALTER TABLE herblocalitygroup ADD CONSTRAINT herblocalitygroup_pk PRIMARY KEY (id);
ALTER TABLE herblocalitygroup ADD CONSTRAINT herblocalitygroup_id_hierarchy_fk FOREIGN KEY (id) REFERENCES hierarchy (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE typespecimengroup DROP CONSTRAINT IF EXISTS typespecimengroup_pk CASCADE;
ALTER TABLE typespecimengroup DROP CONSTRAINT IF EXISTS typespecimengroup_id_hierarchy_fk CASCADE;
ALTER TABLE typespecimengroup RENAME TO herbtypespecimengroup;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenKind TO herbTypeSpecimenKind;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenAssertionBy TO herbTypeSpecimenAssertionBy;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenReference TO herbTypeSpecimenReference;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenRefPage TO herbTypeSpecimenRefPage;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenBasionym TO herbTypeSpecimenBasionym;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenNotes TO herbTypeSpecimenNotes;
ALTER TABLE herbtypespecimengroup ADD CONSTRAINT herbtypespecimengroup_pk PRIMARY KEY (id);
ALTER TABLE herbtypespecimengroup ADD CONSTRAINT herbtypespecimengroup_id_hierarchy_fk FOREIGN KEY (id) REFERENCES hierarchy (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE hybridparentgroup DROP CONSTRAINT IF EXISTS hybridparentgroup_pk CASCADE;
ALTER TABLE hybridparentgroup DROP CONSTRAINT IF EXISTS hybridparentgroup_id_hierarchy_fk CASCADE;
ALTER TABLE hybridparentgroup RENAME to herbHybridParentGroup;
ALTER TABLE herbHybridParentGroup RENAME COLUMN hybridParent TO herbHybridParent;
ALTER TABLE herbHybridParentGroup RENAME COLUMN hybridParentQualifier TO herbHybridParentQualifier;
ALTER TABLE herbHybridParentGroup ADD CONSTRAINT herbHybridParentGroup_pk PRIMARY KEY (id);
ALTER TABLE herbHybridParentGroup ADD CONSTRAINT herbHybridParentGroup_id_hierarchy_fk FOREIGN KEY (id) REFERENCES hierarchy (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersjan TYPE CHARACTER VARYING USING CAST(flowersjan AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersfeb TYPE CHARACTER VARYING USING CAST(flowersfeb AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersmar TYPE CHARACTER VARYING USING CAST(flowersmar AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersapr TYPE CHARACTER VARYING USING CAST(flowersapr AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersmay TYPE CHARACTER VARYING USING CAST(flowersmay AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersjun TYPE CHARACTER VARYING USING CAST(flowersjun AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersjul TYPE CHARACTER VARYING USING CAST(flowersjul AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersaug TYPE CHARACTER VARYING USING CAST(flowersaug AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowerssep TYPE CHARACTER VARYING USING CAST(flowerssep AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersoct TYPE CHARACTER VARYING USING CAST(flowersoct AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersnov TYPE CHARACTER VARYING USING CAST(flowersnov AS CHARACTER);
ALTER TABLE collectionobjects_accessionattributes ALTER COLUMN flowersdec TYPE CHARACTER VARYING USING CAST(flowersdec AS CHARACTER);