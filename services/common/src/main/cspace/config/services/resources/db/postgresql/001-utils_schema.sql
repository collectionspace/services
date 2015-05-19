-- Create the utils schema, views will go there
DO $DO$
BEGIN
   IF NOT EXISTS ( SELECT 1
      FROM
         pg_catalog.pg_namespace n
      WHERE n.nspname = 'utils' )
   THEN
      CREATE SCHEMA utils AUTHORIZATION nuxeo_botgarden;
      GRANT ALL ON SCHEMA utils TO nuxeo_botgarden;
      GRANT ALL ON SCHEMA utils TO reporters_botgarden;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT ALL ON TABLES TO nuxeo_botgarden;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT SELECT ON TABLES TO reporters_botgarden;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT ALL ON FUNCTIONS TO nuxeo_botgarden;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT EXECUTE ON FUNCTIONS TO reporters_botgarden;
   END IF;
END$DO$;
