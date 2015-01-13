-- A wrapper script that runs custom SQL statements at startup time
--    Aggregating the sql scripts into this single file makes it possible
--    to specify the order of execution.

-- Create the utils schema, views will go there
DO $DO$
BEGIN
   IF NOT EXISTS ( SELECT 1
      FROM
         pg_catalog.pg_namespace n
      WHERE n.nspname = 'utils' )
   THEN
      CREATE SCHEMA utils AUTHORIZATION nuxeo_botgarden;
      GRANT USAGE ON SCHEMA utils TO PUBLIC;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
         GRANT SELECT ON TABLES TO PUBLIC;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT EXECUTE ON FUNCTIONS TO PUBLIC;
   END IF;
END$DO$;
