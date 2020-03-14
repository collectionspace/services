-- Upgrade the annotation collectionobject extension. Change the type of some dates to timestamp
-- without timezone instead of varchar (DRYD-812).

ALTER TABLE annotationgroup
  ALTER COLUMN annotationdate TYPE TIMESTAMP WITHOUT TIME ZONE
  USING annotationdate::timestamp without time zone;

ALTER TABLE accessionusegroup
  ALTER COLUMN accessionusefilleddate TYPE TIMESTAMP WITHOUT TIME ZONE
  USING accessionusefilleddate::timestamp without time zone;

ALTER TABLE accessionusegroup
  ALTER COLUMN accessionuserequestdate TYPE TIMESTAMP WITHOUT TIME ZONE
  USING accessionuserequestdate::timestamp without time zone;
