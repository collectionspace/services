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
      CREATE SCHEMA utils AUTHORIZATION nuxeo;
      GRANT USAGE ON SCHEMA utils TO PUBLIC;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
         GRANT SELECT ON TABLES TO PUBLIC;
      ALTER DEFAULT PRIVILEGES IN SCHEMA utils
        GRANT EXECUTE ON FUNCTIONS TO PUBLIC;
   END IF;
END$DO$;


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
      ALTER TYPE voucherlabeltype OWNER TO nuxeo;
   EXCEPTION
      WHEN duplicate_object THEN
         RAISE NOTICE 'NOTICE: voucherlabeltype already exists';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating type voucherlabeltype: (%)', SQLSTATE;
   END;
END$DO$;


-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the hierarchy table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'hierarchy'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The hierarchy table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'hierarchy_name_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX hierarchy_name_idx ON hierarchy (name);
   ALTER INDEX hierarchy_name_idx OWNER to nuxeo;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'hierarchy_pos_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX hierarchy_pos_idx ON hierarchy (pos);
   ALTER INDEX hierarchy_pos_idx OWNER to nuxeo;
END IF;
END$$;


-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the misc table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'misc'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The misc table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'misc_lifecyclestate_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX misc_lifecyclestate_idx ON misc (lifecyclestate) ;
   ALTER INDEX misc_lifecyclestate_idx OWNER TO nuxeo;
END IF;
END$$;



-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the collectionobjects_botgarden  table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'collectionobjects_botgarden'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The collectionobjects_botgarden table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'collectionobjects_botgarden_deadflag_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX collectionobjects_botgarden_deadflag_idx ON collectionobjects_botgarden (deadflag);
   ALTER INDEX collectionobjects_botgarden_deadflag_idx OWNER TO nuxeo;
END IF;
END$$;



-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the collectionobjects_naturalhistory table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'collectionobjects_naturalhistory'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The collectionobjects_naturalhistory table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'collectionobjects_naturalhistory_rare_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX collectionobjects_naturalhistory_rare_idx ON collectionobjects_naturalhistory (rare);
   ALTER INDEX collectionobjects_naturalhistory_rare_idx OWNER TO nuxeo;
END IF;
END$$;



-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the relations_common table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'relations_common'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The relations_common table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'relations_common_objectdocumenttype_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX relations_common_objectdocumenttype_idx ON relations_common (objectdocumenttype);
   ALTER INDEX relations_common_objectdocumenttype_idx OWNER TO nuxeo;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'relations_common_subjectdocumenttype_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX relations_common_subjectdocumenttype_idx ON relations_common (subjectdocumenttype);
   ALTER INDEX relations_common_subjectdocumenttype_idx OWNER to nuxeo;
END IF;
END$$;



-- Botgarden tenant database add-ons, 
-- indexes, grants, functions for the taxonomicIdentGroup table

DO $$
BEGIN

IF NOT EXISTS ( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'taxonomicidentgroup'
      AND c.relkind = 'r'
      AND n.nspname = 'public' )
THEN
   RAISE NOTICE 'NOTICE: The taxonomicIdentGroup table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'taxonomicidentgroup_taxon_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX taxonomicIdentGroup_taxon_idx ON taxonomicIdentGroup (taxon);
   ALTER INDEX taxonomicIdentGroup_taxon_idx OWNER TO nuxeo;
END IF;
END$$;


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
      ALTER FUNCTION getdispl(in text) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION getdispl(in text) to public;
   EXCEPTION
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function getdispl: (%)', SQLSTATE;
   END;
END$DO$;


-- 
DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findhybridaffinhtml (tigid varchar)
         RETURNS varchar AS
      $CF$
      DECLARE
         taxon_refname varchar(300);
         taxon_name varchar(200);
         taxon_name_form varchar(300);
         is_hybrid boolean;
         aff_refname varchar(300);
         aff_name varchar(200);
         aff_name_form varchar(300);
         aff_genus varchar(100);
         fhp_name varchar(200);
         fhp_genus varchar(100);
         mhp_name varchar(200);
         mhp_genus varchar(100);
         mhp_rest varchar(200);
         return_name varchar(300);

      BEGIN
         SELECT INTO
            taxon_refname,
            taxon_name,
            is_hybrid,
            aff_refname,
            aff_name,
            aff_genus
            tig.taxon,
            regexp_replace(tig.taxon, '^.*\)''(.+)''$', '\1'),
            tig.hybridflag,
            tig.affinitytaxon,
            regexp_replace(tig.affinitytaxon, '^.*\)''(.+)''$', '\1'),
            regexp_replace(tig.affinitytaxon, '^.*\)''([^ ]+)( ?.*)''$', '\1')
         FROM taxonomicidentgroup tig
         WHERE tig.id = $1;

         IF NOT FOUND THEN
            RETURN NULL;
         ELSEIF is_hybrid IS FALSE AND aff_name IS NULL THEN
            SELECT INTO taxon_name_form ttg.termformatteddisplayname
            FROM
               taxonomicidentgroup tig
               INNER JOIN taxon_common tc ON (tig.taxon = tc.refname)
               INNER JOIN hierarchy h
                  ON (tc.id = h.parentid
                      AND h.primarytype = 'taxonTermGroup')
               INNER JOIN taxontermgroup ttg
                  ON (h.id = ttg.id
                      AND taxon_name = ttg.termdisplayname)
            WHERE ttg.termformatteddisplayname IS NOT NULL
                  AND tig.id = $1;

            RETURN taxon_name_form;

         ELSEIF is_hybrid is false AND aff_name IS NOT NULL THEN
            SELECT INTO aff_name_form
               regexp_replace(ttg.termformatteddisplayname,
                  '^(<i>[^ ]+)( ?)(.*</i>.*)$', '\1</i> aff.\2<i>\3')
            FROM
               taxonomicidentgroup tig
               INNER JOIN taxon_common tc
                  ON (tig.affinitytaxon = tc.refname)
               INNER JOIN hierarchy h
                  ON (tc.id = h.parentid
                      AND h.primarytype = 'taxonTermGroup')
               INNER JOIN taxontermgroup ttg
                  ON (h.id = ttg.id
                      AND aff_name = ttg.termdisplayname)
            WHERE
               ttg.termformatteddisplayname IS NOT NULL
               AND tig.id = $1;

            RETURN aff_name_form; 

         ELSEIF is_hybrid IS TRUE THEN
            SELECT INTO fhp_name, fhp_genus
               CASE WHEN fhp.taxonomicidenthybridparent IS NULL THEN ''
               ELSE ttg.termformatteddisplayname
               END,
               CASE WHEN fhp.taxonomicidenthybridparent IS NULL THEN ''
               ELSE regexp_replace(fhp.taxonomicidenthybridparent,
                    '^.*\)''([^ ]+)( ?.*)''$', '\1')
               END
            FROM
               taxonomicidentgroup tig
               INNER JOIN hierarchy hfhp
                  ON (hfhp.parentid = tig.id
                      AND hfhp.name = 'taxonomicIdentHybridParentGroupList')
               INNER JOIN taxonomicidenthybridparentgroup fhp
                  ON (hfhp.id = fhp.id
                      AND fhp.taxonomicidenthybridparentqualifier = 'female')
               INNER JOIN taxon_common tc
                  ON (fhp.taxonomicidenthybridparent = tc.refname)
               INNER JOIN hierarchy h
                  ON (tc.id = h.parentid
                      AND h.primarytype = 'taxonTermGroup')
               INNER JOIN taxontermgroup ttg
                  ON (h.id = ttg.id
                      AND regexp_replace(fhp.taxonomicidenthybridparent,
                            '^.*\)''(.+)''$', '\1') = ttg.termdisplayname)
            WHERE
               ttg.termformatteddisplayname IS NOT NULL
               AND tig.id = $1;

            SELECT into mhp_name, mhp_genus, mhp_rest
               CASE when mhp.taxonomicidenthybridparent IS NULL THEN ''
               ELSE ttg.termformatteddisplayname
               END,
               CASE when mhp.taxonomicidenthybridparent IS NULL THEN ''
               ELSE regexp_replace(mhp.taxonomicidenthybridparent,
                       '^.*\)''([^ ]+)( .*)''$', '\1')
               END,
               CASE when mhp.taxonomicidenthybridparent IS NULL THEN ''
               ELSE regexp_replace(ttg.termformatteddisplayname,
                       '^[Xx×]? ?<i>[^ ]+( ?.*)$', '\1')
               END
            FROM
               taxonomicidentgroup tig
               INNER JOIN hierarchy hmhp
                  ON (hmhp.parentid = tig.id
                      AND hmhp.name = 'taxonomicIdentHybridParentGroupList')
               INNER JOIN taxonomicidenthybridparentgroup mhp
                  ON (hmhp.id = mhp.id
                      AND mhp.taxonomicidenthybridparentqualifier = 'male')
               INNER JOIN taxon_common tc
                  ON (mhp.taxonomicidenthybridparent = tc.refname)
               INNER JOIN hierarchy h
                  ON (tc.id = h.parentid
                      AND h.primarytype = 'taxonTermGroup')
               INNER JOIN taxontermgroup ttg
                  ON (h.id = ttg.id
                      AND regexp_replace(mhp.taxonomicidenthybridparent,
                             '^.*\)''(.+)''$', '\1') = ttg.termdisplayname)
            WHERE
               ttg.termformatteddisplayname IS NOT NULL
               AND tig.id = $1;

            IF aff_name IS NULL THEN
               IF fhp_genus = mhp_genus THEN
                  return_name := trim(fhp_name || ' × ' ||
                     '<i>' || substr(mhp_genus, 1, 1) || '.' || mhp_rest);
               ELSE
                  return_name := trim(fhp_name || ' × ' || mhp_name);
               END IF;
            ELSE
               IF aff_genus = mhp_genus THEN
                  return_name := trim(aff_name_form || ' × ' ||
                     '<i>' || substr(mhp_genus, 1, 1) || '.' || mhp_rest);
               ELSE
                  return_name := trim(aff_name_form || ' × ' || mhp_name);
               END IF;
            END IF;

            IF return_name = ' × ' THEN
               RETURN NULL;
            ELSE
               RETURN return_name;
            END IF;
         END IF;
      END;
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findhybridaffinhtml (tigid varchar) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findhybridaffinhtml(tgid varchar) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: creating function findhybridaffinhtml: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findhybridaffinhtml: (%)', SQLSTATE;
   END;
END$DO$;


DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findhybridaffinname (tigid VARCHAR)
      RETURNS VARCHAR AS
      $CF$
         DECLARE
            taxon_name VARCHAR(200);
            is_hybrid boolean;
            aff_name VARCHAR(200);
            aff_genus VARCHAR(100);
            fhp_name VARCHAR(200);
            fhp_genus VARCHAR(100);
            mhp_name VARCHAR(200);
            mhp_genus VARCHAR(100);
            mhp_rest VARCHAR(200);
            return_name VARCHAR(300);
         
         BEGIN
         SELECT INTO
            taxon_name,
            is_hybrid,
            aff_name,
            aff_genus
            regexp_replace(tig.taxon, '^.*\)''(.+)''$', '\1'),
            tig.hybridflag, 
            regexp_replace(tig.affinitytaxon, '^.*\)''([^ ]+)( ?.*)''$', '\1 aff.\2'),
            regexp_replace(tig.affinitytaxon, '^.*\)''([^ ]+)( ?.*)''$', '\1')
         FROM
            taxonomicidentgroup tig
         WHERE
            tig.id = $1;
         
         IF NOT FOUND THEN
            RETURN NULL;
         ELSEIF is_hybrid IS FALSE AND aff_name IS NULL THEN
            RETURN taxon_name;
         ELSEIF is_hybrid IS FALSE AND aff_name IS NOT NULL THEN
            RETURN aff_name;
         ELSEIF is_hybrid is true THEN
            SELECT INTO fhp_name, fhp_genus
               CASE WHEN fhp.taxonomicidenthybridparent IS NULL THEN ''
                  ELSE regexp_replace(fhp.taxonomicidenthybridparent,
                     '^.*\)''(.+)''$', '\1')
               END,
               CASE WHEN fhp.taxonomicidenthybridparent IS NULL THEN ''
                  ELSE regexp_replace(fhp.taxonomicidenthybridparent,
                     '^.*\)''([^ ]+) ?.*''$', '\1')
               END
            FROM
               taxonomicidentgroup tig
               INNER JOIN hierarchy hfhp
                  ON (hfhp.parentid = tig.id 
                      AND hfhp.name = 'taxonomicIdentHybridParentGroupList')
               INNER JOIN taxonomicidenthybridparentgroup fhp
                  ON (hfhp.id = fhp.id 
                      AND fhp.taxonomicidenthybridparentqualifier = 'female')
            WHERE tig.id = $1;
         
            SELECT INTO mhp_name, mhp_genus, mhp_rest
               CASE WHEN mhp.taxonomicidenthybridparent IS NULL THEN ''
                  ELSE regexp_replace(mhp.taxonomicidenthybridparent,
                     '^.*\)''(.+)''$', '\1')
               END,
               CASE WHEN mhp.taxonomicidenthybridparent IS NULL THEN ''
                  ELSE regexp_replace(mhp.taxonomicidenthybridparent,
                     '^.*\)''([^ ]+) ?.*''$', '\1')
               END,
               CASE WHEN mhp.taxonomicidenthybridparent IS NULL THEN ''
                  ELSE regexp_replace(mhp.taxonomicidenthybridparent,
                     '^.*\)''([^ ]+)( ?.*)''$', '\2')
               END
            FROM
               taxonomicidentgroup tig
               INNER JOIN hierarchy hmhp
                  ON (hmhp.parentid = tig.id 
                      AND hmhp.name = 'taxonomicIdentHybridParentGroupList')
               INNER JOIN taxonomicidenthybridparentgroup mhp
                  ON (hmhp.id = mhp.id 
                      AND mhp.taxonomicidenthybridparentqualifier = 'male')
            WHERE tig.id = $1;
         
            IF aff_name IS NULL THEN
               IF fhp_genus = mhp_genus THEN
                  return_name := trim(fhp_name || ' × ' || 
                     substr(mhp_genus, 1, 1) || '.' || mhp_rest);
               ELSE
                  return_name := trim(fhp_name || ' × ' || mhp_name);
               END IF;
            ELSE 
               IF aff_genus = mhp_genus THEN
                  return_name := trim(aff_name || ' × ' || 
                     substr(mhp_genus, 1, 1) || '.' || mhp_rest);
               ELSE
                  return_name := trim(aff_name || ' × ' || mhp_name);
               END IF;
            END IF;
         
            IF return_name = ' × ' THEN
               RETURN NULL;
            ELSE
               RETURN return_name;
            END IF;
         END IF;
      END;
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findhybridaffinname (tigid VARCHAR) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findhybridaffinname(tgid VARCHAR) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: creating function findhybridaffinname: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findhybridaffinname: (%)', SQLSTATE;
   END;
END$DO$;


-- Create findsectionparent function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findsectionparent(character varying) RETURNS character varying AS
      $CF$
         SELECT regexp_replace(tc2.refname, '^.*\)''(.*)''$', '\1') sectionparent
         FROM taxon_common tc1
         JOIN hierarchy h1 ON tc1.id = h1.id
         JOIN relations_common rc1 ON (h1.name = rc1.subjectcsid
                                       AND rc1.relationshiptype = 'hasBroader'
                                       AND rc1.subjectdocumenttype = 'Taxon')
         JOIN hierarchy h2 ON (h2.name = rc1.objectcsid)
         JOIN taxon_common tc2 ON (tc2.id = h2.id)
         WHERE tc2.taxonrank = 'section'
         AND tc1.refname = $1
      $CF$
      LANGUAGE SQL IMMUTABLE RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findsectionparent(character varying) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findsectionparent(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: creating function findsectionparent: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findsectionparent: (%)', SQLSTATE;
   END;
END$DO$;


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
      ALTER FUNCTION findsubsectionparent(character varying) OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findsubsectionparent(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: Creating function findsubsectionparent: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findsubsectionparent : (%)', SQLSTATE;
   END;
END$DO$;


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


-- Create the findvoucherlabels function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

-- used by voucher label report to use number of sheets to print multiple voucher labels
-- CRH 2/2/2013

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findvoucherlabels() RETURNS setof voucherlabeltype AS
      $CF$
      DECLARE
          sheetcount integer;
          r voucherlabeltype%rowtype;
          n integer;
      
      BEGIN
      
      FOR r IN
      select co1.objectnumber,
      findhybridaffinhtml(tig.id) determinationformatted,
      case when (tn.family is not null and tn.family <> '')
           then regexp_replace(tn.family, '^.*\)''(.*)''$', '\1')
      end as family,
      case when fc.item is not null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is not null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber||', '||sdg.datedisplaydate
        when fc.item is not null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber||', s.d.'
        when fc.item is not null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is not null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' s.n., '||sdg.datedisplaydate
        when fc.item is not null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' s.n., s.d.'
        when fc.item is null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is not null
              then co1.fieldcollectionnumber||', '||sdg.datedisplaydate
        when fc.item is null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is null
              then co1.fieldcollectionnumber||', s.d.'
        when fc.item is null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is not null
              then 's.n., '||sdg.datedisplaydate
      end as collectioninfo,
      lc.loanoutnumber vouchernumber,
      lnh.numlent numbersheets,
      lb.labelrequested,
      case when (lb.gardenlocation is not null and lb.gardenlocation <> '')
           then 'Garden No. '||co1.objectnumber||', Bed '||regexp_replace(lb.gardenlocation, '^.*\)''(.*)''$', '\1')
           else 'Garden No. '||co1.objectnumber||', Bed unknown'
      end as gardeninfo,
      case when lb.hortwild='Horticultural' then 'Horticultural voucher:'
           when lb.hortwild='Wild' then 'Wild voucher:'
      end as vouchertype,
      lb.fieldcollectionnote,
      lb.annotation,
      case when (lbc.item is not null and lbc.item <> '' and lc.loanoutdate is not null)
            then regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')||', '||to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')
           when (lbc.item is not null and lbc.item <> '' and lc.loanoutdate is null)
            then regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')
           when (lbc.item is null and lc.loanoutdate is not null)
            then to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')
      end as vouchercollectioninfo
      from loansout_common lc
      join loansout_naturalhistory lnh on (lc.id=lnh.id)
      join loansout_botgarden lb on (lc.id=lb.id)
      left outer join loansout_botgarden_collectorlist lbc on (lbc.id = lc.id and lbc.pos=0)
      
      join hierarchy h1 on lc.id=h1.id
      join relations_common r1 on (h1.name=r1.subjectcsid and objectdocumenttype='CollectionObject')
      join hierarchy h2 on (r1.objectcsid=h2.name)
      join collectionobjects_common co1 on (co1.id=h2.id)
      
      left outer join hierarchy htig
           on (co1.id = htig.parentid and htig.pos = 0 and htig.name = 'collectionobjects_naturalhistory:taxonomicIdentGroupList')
      left outer join taxonomicIdentGroup tig on (tig.id = htig.id)
      
      join collectionspace_core core on (core.id=co1.id)
      join misc misc2 on (misc2.id = co1.id and misc2.lifecyclestate <> 'deleted')
      
      left outer join taxon_common tc on (tig.taxon=tc.refname)
      left outer join taxon_naturalhistory tn on (tc.id=tn.id)
      
      left outer join hierarchy htt
          on (tc.id=htt.parentid and htt.name='taxon_common:taxonTermGroupList' and htt.pos=0)
      left outer join taxontermgroup tt on (tt.id=htt.id)
      
      left outer join collectionobjects_common_fieldCollectors fc on (co1.id = fc.id and fc.pos = 0)
      
      left outer join hierarchy hfcdg on (co1.id = hfcdg.parentid  and hfcdg.name='collectionobjects_common:fieldCollectionDateGroup')
      left outer join structureddategroup sdg on (sdg.id = hfcdg.id)
      
      where lb.labelrequested = 'Yes'
      order by objectnumber
      
      LOOP
      
      -- return next r;
      
      sheetcount := r.numbersheets;
      
      for n in 1..sheetcount loop
      
      return next r;
      
      END LOOP;
      
      END LOOP;
      
      RETURN;
      END; 
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findvoucherlabels() OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findvoucherlabels() to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: While creating function findvoucherlabels: missing relation';
      WHEN undefined_object THEN
         RAISE NOTICE 'NOTICE: While creating function findvoucherlabels: undefined object (probably voucherlabeltype)';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findvoucherlabels: (%)', SQLSTATE;
   END;
END$DO$;


-- A view used for creating plant tag records
DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE VIEW utils.plantsalespottags AS
      SELECT
         row_number() OVER (ORDER BY pc.id) AS label_id, 
         co1.objectnumber AS accession_number, 
         CASE
            WHEN pc.taxonname IS NOT NULL AND pc.family IS NOT NULL THEN
              (getdispl(pc.taxonname) || ' ') || getdispl(pc.family)
            WHEN pc.taxonname IS NOT NULL AND pc.family IS NULL THEN
               getdispl(pc.taxonname)
            WHEN pc.taxonname IS NULL AND pc.family IS NOT NULL THEN
               getdispl(pc.family)
            ELSE NULL
         END AS taxon_data, 
         pc.commonname AS common_name,
         pc.locale AS country_name, 
         pc.labeldata AS label_data,
         pc.numberoflabels AS quantity, 
         pc.printlabels AS label_req_flag,
         core.createdby AS staff_logon, 
         date(core.createdat) AS date_entered, 
         core.updatedby AS last_change_staff_logon, 
         date(core.updatedat) AS last_change_date
      FROM
         pottags_common pc
         LEFT JOIN hierarchy h1
            ON pc.id = h1.id
         LEFT JOIN relations_common r1
            ON (h1.name = r1.subjectcsid
                AND r1.objectdocumenttype = 'CollectionObject')
         LEFT JOIN hierarchy h2
            ON r1.objectcsid = h2.name
         LEFT JOIN collectionobjects_common co1
            ON co1.id = h2.id
         JOIN collectionspace_core core
            ON (core.id = pc.id
                AND core.tenantid = 35)
         JOIN misc misc1
            ON (misc1.id = pc.id
                AND misc1.lifecyclestate <> 'deleted')
         LEFT JOIN misc misc2
            ON (misc2.id = co1.id
                AND misc2.lifecyclestate <> 'deleted')
      WHERE pc.printlabels = 'yes'
      ORDER BY row_number() OVER (ORDER BY pc.id);
      ALTER VIEW utils.plantsalespottags OWNER TO nuxeo;
      GRANT SELECT ON utils.plantsalespottags to PUBLIC;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: Creating view utils.plantsalespottags: missing relation';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function utils.plantsalespottags: (%)', SQLSTATE;
   END;
END$DO$;

