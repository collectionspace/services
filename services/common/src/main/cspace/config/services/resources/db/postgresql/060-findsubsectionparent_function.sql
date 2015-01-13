
-- Create the findsubsectionparent function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findsubsectionparent(character varying) RETURNS character varying AS
      $CF$
         SELECT
            regexp_replace(tc2.refname, '^.*\)''(.*)''$', '\1') subsectionparent
         FROM
            taxon_common tc1
            JOIN hierarchy h1 ON tc1.id=h1.id
            JOIN relations_common rc1 ON (h1.name=rc1.subjectcsid
                                          AND rc1.relationshiptype='hasBroader'
                                          AND rc1.subjectdocumenttype='Taxon')
            JOIN hierarchy h2 ON h2.name=rc1.objectcsid
            JOIN taxon_common tc2 ON tc2.id=h2.id
         WHERE
            tc2.taxonrank IN ('subsection', 'subsect')
            AND tc1.refname=$1
      $CF$
      LANGUAGE SQL IMMUTABLE RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findsubsectionparent(character varying) OWNER TO nuxeo_botgarden;
      GRANT EXECUTE ON FUNCTION findsubsectionparent(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: Creating function findsubsectionparent: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findsubsectionparent : (%)', SQLSTATE;
   END;
END$DO$;

