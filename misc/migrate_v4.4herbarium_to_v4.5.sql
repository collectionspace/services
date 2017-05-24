-- db1: {herbarium45_default localhost 5432 csadmin cs1dn3b sslmode=disable}
-- db2: {herbarium_default localhost 5432 csadmin cs1dn3b sslmode=disable}
-- Run the following SQL againt db2:
DROP TABLE IF EXISTS collectionobjects_naturalhistory;

-- Rename these tables and corresponding columns
ALTER TABLE associatedtaxagroup RENAME TO herbassociatedtaxagroup;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxon TO herbAssocTaxon;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxonCommonName TO herbAssocTaxonCommonName;
ALTER TABLE herbassociatedtaxagroup RENAME COLUMN assocTaxonInteraction TO herbAssocTaxonInteraction;

ALTER TABLE localitygroup RENAME TO herblocalitygroup;
ALTER TABLE herblocalitygroup RENAME COLUMN collectionLocationVerbatim TO herbCollectionLocationVerbatim;
ALTER TABLE herblocalitygroup RENAME COLUMN collectionPlace TO herbCollectionPlace;

ALTER TABLE typespecimengroup RENAME TO herbtypespecimengroup;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenKind TO herbTypeSpecimenKind;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenAssertionBy TO herbTypeSpecimenAssertionBy;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenReference TO herbTypeSpecimenReference;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenRefPage TO herbTypeSpecimenRefPage;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenBasionym TO herbTypeSpecimenBasionym;
ALTER TABLE herbtypespecimengroup RENAME COLUMN typeSpecimenNotes TO herbTypeSpecimenNotes;

ALTER TABLE hybridparentgroup RENAME to herbHybridParentGroup;
ALTER TABLE herbHybridParentGroup RENAME COLUMN hybridParent TO herbHybridParent;
ALTER TABLE herbHybridParentGroup RENAME COLUMN hybridParentQualifier TO herbHybridParentQualifier;
