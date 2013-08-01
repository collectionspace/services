
-- Create the findcommonname function
--   Parses out the commonname part of naturalhistorycommonname, a refname
--   from the naturalhistorycommonnamegroup table.
--   The search arg is a taxon_common.refname
--
--   Used by the botgarden tenant

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findcommonname(character varying) RETURNS character varying AS
      $CF$
         SELECT regexp_replace(cng.naturalhistorycommonname, '^.*\)''(.*)''$', '\1') commonname
         FROM taxon_common tc
              LEFT OUTER JOIN hierarchy h
                 ON (tc.id = h.parentid
                     AND h.pos = 0
                     AND h.name = 'taxon_naturalhistory:naturalHistoryCommonNameGroupList')
              LEFT OUTER JOIN naturalhistorycommonnamegroup cng
                 ON (cng.id = h.id)
         WHERE tc.refname=$1       
      $CF$
      LANGUAGE SQL IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findcommonname(character varying) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findcommonname(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: Creating function findcommonname: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findcommonname: (%)', SQLSTATE;
   END;
END$DO$;

