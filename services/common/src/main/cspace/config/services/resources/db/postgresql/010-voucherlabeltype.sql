--  Type declarations go first as they usually have no other dependencies
DO $DO$
BEGIN
   BEGIN
      CREATE TYPE voucherlabeltype AS
      (
         objectnumber varchar,
         determinationformatted varchar,
         family varchar,
         collectioninfo varchar,
         vouchernumber varchar,
         numbersheets integer,
         labelrequested varchar,
         gardeninfo varchar,
         vouchertype varchar,
         fieldcollectionnote varchar,
         annotation varchar,
         vouchercollectioninfo varchar
      );
      ALTER TYPE voucherlabeltype OWNER TO nuxeo_botgarden;
   EXCEPTION
      WHEN duplicate_object THEN
         RAISE NOTICE 'NOTICE: voucherlabeltype already exists';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating type voucherlabeltype: (%)', SQLSTATE;
   END;
END$DO$;
