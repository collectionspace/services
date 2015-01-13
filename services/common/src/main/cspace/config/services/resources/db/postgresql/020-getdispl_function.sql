-- A convenience function, extracts displayname from a refname

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION getdispl(in text) RETURNS text AS
      $$
        SELECT regexp_replace($1, '^.*\)''(.*)''$', '\1')
      $$
      LANGUAGE SQL IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION getdispl(in text) OWNER TO nuxeo_botgarden;
      GRANT EXECUTE ON FUNCTION getdispl(in text) to public;
   EXCEPTION
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function getdispl: (%)', SQLSTATE;
   END;
END$DO$;


