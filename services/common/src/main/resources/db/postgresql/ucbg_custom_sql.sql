-- A wrapper script that runs custom SQL statements at startup time
--    Aggregating the sql scripts into this single file makes it possible
--    to specify the order of execution.


--  Type declarations go first as they usually have no other dependencies
DO $DOS$
BEGIN
   BEGIN
      CREATE TYPE voucherlabeltype AS
      (
         objectnumber varchar,
         determinationformatted varchar,
         family varchar,
         collectioninfo varchar,
         locality varchar,
         vouchernumber varchar,
         numbersheets integer,
         labelrequested varchar,
         gardenlocation varchar,
         gardeninfo varchar,
         vouchertype varchar,
         fieldcollectionnote varchar,
         annotation varchar,
         vouchercollectioninfo varchar
      );
   EXCEPTION
      WHEN duplicate_object THEN
         RAISE NOTICE 'INFO voucherlabeltype already exists';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating type voucherlabeltype: (%)', SQLSTATE;
   END;
END$DOS$;


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
   RAISE NOTICE 'The hierarchy table is missing';
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
   RAISE NOTICE 'The misc table is missing';
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
   RAISE NOTICE 'The collectionobjects_botgarden table is missing';
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
   RAISE NOTICE 'The collectionobjects_naturalhistory table is missing';
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
   RAISE NOTICE 'The relations_common table is missing';
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
   RAISE NOTICE 'The taxonomicIdentGroup table is missing';
   RETURN;
END IF;

IF NOT EXISTS( SELECT 1
   FROM
      pg_class c JOIN pg_namespace n ON (n.oid = c.relnamespace)
   WHERE
      c.relname = 'taxonomicIdentGroup_taxon_idx'
      AND
      c.relkind = 'i'
      AND
      n.nspname = 'public' )
THEN
   CREATE INDEX taxonomicIdentGroup_taxon_idx ON taxonomicIdentGroup (taxon);
END IF;
END$$;



-- Create findhybridname function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

-- Used in reports to construct a combined hybrid name from the hybrid parents
-- CRH 1/15/2013
-- from findhybridnamehtml though simpler; remove joins to get taxonterm fields


DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findhybridname(character varying) RETURNS character varying AS
      $CF$
      DECLARE
         numtimes integer;
         htmlname text;
         strresult text;
         femalename text;
         malename text;
         parentgender text;

      BEGIN

      htmlname := '';
      strresult := '';

      SELECT INTO numtimes count(*)
      FROM
         public.taxonomicidentgroup tig
         LEFT OUTER JOIN
         hierarchy h ON (h.parentid=tig.id
                         AND
                         h.name='taxonomicIdentHybridParentGroupList')
         LEFT OUTER JOIN
         taxonomicidenthybridparentgroup thpg ON (h.id=thpg.id)
      WHERE
          tig.id = $1;

      IF numtimes > 1
      THEN
         FOR htmlname IN
            SELECT
               regexp_replace(thpg.taxonomicidenthybridparent, '^.*\)''(.*)''$', '\1')
            FROM
               public.taxonomicidentgroup tig
               LEFT OUTER JOIN
               hierarchy h ON (h.parentid=tig.id
                               AND
                               h.name='taxonomicIdentHybridParentGroupList')
               LEFT OUTER JOIN
               taxonomicidenthybridparentgroup thpg on (h.id=thpg.id)
            WHERE tig.id = $1
            ORDER BY taxonomicidenthybridparentqualifier
         LOOP
            strresult := strresult || htmlname || ' × ';
         END LOOP;

         strresult := trim (trailing ' × ' from strresult);

      ELSIF numtimes = 1
      THEN
         SELECT INTO htmlname, parentgender
            regexp_replace(thpg.taxonomicidenthybridparent, '^.*\)''(.*)''$', '\1'),
            thpg.taxonomicidenthybridparentqualifier
         FROM
            public.taxonomicidentgroup tig
            LEFT OUTER JOIN
            hierarchy h ON (h.parentid=tig.id
                            AND
                            h.name='taxonomicIdentHybridParentGroupList')
            LEFT OUTER JOIN
            taxonomicidenthybridparentgroup thpg ON (h.id=thpg.id)
         WHERE
            tig.id = $1;

         -- if parentqualifier = 'female' then print htmlname||' × '
         IF parentgender = 'female' THEN
            strresult := htmlname||' ×';

         -- if parentqualifier = 'male' then print ' × '||htmlname
         ELSIF parentgender = 'male' THEN
            strresult := '× '||htmlname;
         END IF;

      ELSIF numtimes = 0
      THEN
         -- fail
         strresult := 'no hybrid parents';
      END IF;

      RETURN strresult;

      END;
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE RETURNS NULL ON NULL INPUT;
      GRANT EXECUTE ON FUNCTION findhybridname(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'ERROR creating function findhybridname: missing relation';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating function findhybridname: (%)', SQLSTATE;
   END;
END$DO$;


-- Create findhybridnamehtml function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

-- Used in reports to construct a combined hybrid name from the hybrid parents
-- CRH 1/15/2013
-- simpler variant in findhybridname.

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findhybridnamehtml(character varying) RETURNS character varying AS
      $CF$
      DECLARE
         numtimes integer;
         htmlname text;
         strresult text;
         femalename text;
         malename text;
         parentgender text;

      BEGIN
         htmlname := '';
         strresult := '';

         SELECT INTO numtimes count(*)
         FROM
            public.taxonomicidentgroup tig
            LEFT OUTER JOIN hierarchy hhyb
               ON (hhyb.parentid=tig.id
                   AND
                   hhyb.name='taxonomicIdentHybridParentGroupList')
            LEFT OUTER JOIN taxonomicidenthybridparentgroup thpg
               ON (hhyb.id=thpg.id)
            LEFT OUTER JOIN taxon_common tc
               ON (thpg.taxonomicidenthybridparent=tc.refname)
            LEFT OUTER JOIN hierarchy htt
               ON (tc.id=htt.parentid
                   AND htt.name='taxon_common:taxonTermGroupList'
                   AND htt.pos=0) -- for now assuming preferred name
            LEFT OUTER JOIN taxontermgroup tt
               ON (tt.id=htt.id)
         WHERE
            tig.id = $1;

         IF numtimes > 1 THEN

            FOR htmlname IN SELECT tt.termformatteddisplayname
            FROM
               public.taxonomicidentgroup tig
               LEFT OUTER JOIN hierarchy hhyb
                  ON (hhyb.parentid=tig.id
                      AND hhyb.name='taxonomicIdentHybridParentGroupList')
               LEFT OUTER JOIN taxonomicidenthybridparentgroup thpg
                  ON (hhyb.id=thpg.id)
               LEFT OUTER JOIN taxon_common tc
                  ON (thpg.taxonomicidenthybridparent=tc.refname)
               LEFT OUTER JOIN hierarchy htt
                  ON (tc.id=htt.parentid
                      AND htt.name='taxon_common:taxonTermGroupList'
                      AND htt.pos=0)
               LEFT OUTER JOIN taxontermgroup tt
                  ON (tt.id=htt.id)
            WHERE
               tig.id = $1
            ORDER BY taxonomicidenthybridparentqualifier
               LOOP
                  strresult := strresult || htmlname || ' × ';
               END LOOP;

            strresult := trim (trailing ' × ' from strresult);

         ELSIF numtimes = 1 THEN

            SELECT INTO htmlname,
                        parentgender tt.termformatteddisplayname,
                        thpg.taxonomicidenthybridparentqualifier
            FROM
               public.taxonomicidentgroup tig
               LEFT OUTER JOIN hierarchy hhyb
                  ON (hhyb.parentid=tig.id
                      AND hhyb.name='taxonomicIdentHybridParentGroupList')
               LEFT OUTER JOIN taxonomicidenthybridparentgroup thpg
                  ON (hhyb.id=thpg.id)
               LEFT OUTER JOIN taxon_common tc
                  ON (thpg.taxonomicidenthybridparent=tc.refname)
               LEFT OUTER JOIN hierarchy htt
                  ON (tc.id=htt.parentid
                      AND htt.name='taxon_common:taxonTermGroupList'
                      AND htt.pos=0)
               LEFT OUTER JOIN taxontermgroup tt
                  ON (tt.id=htt.id)
            WHERE
               tig.id = $1;

            -- if parentqualifier = 'female' then print htmlname||' × '
            IF parentgender = 'female' THEN
               strresult := htmlname||' ×';

            -- if parentqualifier = 'male' then print ' × '||htmlname
            ELSIF parentgender = 'male' THEN
               strresult := '× '||htmlname;

            END IF;

         ELSIF numtimes = 0 THEN
            -- fail
            strresult := 'no hybrid parents';
         END IF;

         RETURN strresult;
      END;
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE RETURNS NULL ON NULL INPUT;
      GRANT EXECUTE ON function findhybridnamehtml(character varying) to public;

   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'ERROR creating function findhybridnamehtml: missing relation';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating function findhybridnamehtml: (%)', SQLSTATE;
   END;
END$DO$;


-- Create findsectionparent function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findsectionparent(character varying) RETURNS character varying AS
      $FUN$
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
      $FUN$
      LANGUAGE SQL IMMUTABLE RETURNS NULL ON NULL INPUT;
      GRANT EXECUTE ON FUNCTION findsectionparent(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'ERROR creating function findsectionparent: missing relation';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating function findsectionparent: (%)', SQLSTATE;
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
      GRANT EXECUTE ON FUNCTION findsubsectionparent(character varying) to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'Creating function findsubsectionparent: missing relation';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating function findsubsectionparent : (%)', SQLSTATE;
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

   IF NOT EXISTS ( SELECT 1
      FROM
         pg_namespace n
         JOIN
         pg_class c1 ON (n.oid = c1.relnamespace AND c1.relkind = 'r'
                         AND n.nspname = 'public')
         JOIN
         pg_class c2 ON (n.oid = c2.relnamespace AND c2.relkind = 'r')
         jOIN
         pg_class c3 ON (n.oid = c3.relnamespace AND c3.relkind = 'r')
      WHERE
         c1.relname = 'hierarchy'
         AND c2.relname = 'taxon_common'
         AND c3.relname = 'naturalhistorycommonnamegroup' )
   THEN
      RAISE NOTICE 'One of the tables referenced in findcommonname is missing';
      RETURN;
   END IF;

   IF NOT EXISTS ( SELECT 1
      FROM
         pg_catalog.pg_proc p JOIN pg_namespace n ON (n.oid = p.pronamespace)
      WHERE
         n.nspname = 'public'
         AND p.proname = 'findcommonname'
         AND pg_function_is_visible(p.oid)
         AND n.nspname <> 'pg_catalog'
         AND n.nspname <> 'information_schema' )
   THEN
      CREATE FUNCTION findcommonname(character varying) RETURNS character varying AS
      'SELECT
         regexp_replace( cn.naturalhistorycommonname, ''^.*\)''''(.*)''''$'', ''\1'') commonname
      FROM
         taxon_common tc LEFT OUTER JOIN hierarchy h
            ON (tc.id = h.parentid
                AND h.pos = 0
                AND h.name = ''taxon_naturalhistory:naturalHistoryCommonNameGroupList'')
         LEFT OUTER JOIN naturalhistorycommonnamegroup cn
            ON (cn.id = h.id AND cn.naturalhistorycommonnametype=''preferred'')
      WHERE
         tc.refname=$1
      '
      LANGUAGE SQL
      IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      GRANT EXECUTE ON FUNCTION findcommonname(character varying) to public;
   END IF;
END$DO$;


-- Create the findvoucherlabels function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

-- used by voucher label report to use number of sheets to print multiple voucher labels
-- CRH 2/2/2013

DO $DOS$
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
         SELECT co1.objectnumber,
         CASE WHEN tig.hybridflag = 'false' THEN tt.termformatteddisplayname
              WHEN tig.hybridflag = 'true' THEN findhybridnamehtml(tig.id)
         END AS determinationformatted,
         CASE WHEN (tn.family is not null AND tn.family <> '')
            THEN regexp_replace(tn.family, '^.*\)''(.*)''$', '\1')
         END AS family,
         CASE
            WHEN fc.item is not null
                 AND co1.fieldcollectionnumber is not null
                 AND sdg.datedisplaydate is not null
            THEN regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber||', '||sdg.datedisplaydate
            WHEN fc.item is not null
                 AND co1.fieldcollectionnumber is not null
                 AND sdg.datedisplaydate is null
            THEN regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber

            WHEN fc.item is not null AND co1.fieldcollectionnumber is null
                 AND sdg.datedisplaydate is not null
            THEN regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||', '||sdg.datedisplaydate

            WHEN fc.item is not null
                 AND co1.fieldcollectionnumber is null
                 AND sdg.datedisplaydate is null
            THEN regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')

            WHEN fc.item is null
                 AND co1.fieldcollectionnumber is not null
                 AND sdg.datedisplaydate is not null
            THEN co1.fieldcollectionnumber||', '||sdg.datedisplaydate

            WHEN fc.item is null
                 AND co1.fieldcollectionnumber is not null
                 AND sdg.datedisplaydate is null
            THEN co1.fieldcollectionnumber

            WHEN fc.item is null
                 AND co1.fieldcollectionnumber is null
                 AND sdg.datedisplaydate is not null
            THEN sdg.datedisplaydate
         END AS collectioninfo,
         CASE
            WHEN (lg.fieldlocplace is not null
                   AND lg.fieldlocplace <> '')
            THEN regexp_replace(lg.fieldlocplace, '^.*\)''(.*)''$', '\1')

            WHEN (lg.fieldlocplace is null
                  AND lg.taxonomicrange is not null)
            THEN 'Geographic range: '||lg.taxonomicrange
         END AS locality,

         lc.loanoutnumber vouchernumber,
         lnh.numlent numbersheets,
         lb.labelrequested,

         CASE
            WHEN (lb.gardenlocation is not null
                  AND lb.gardenlocation <> '')
            THEN regexp_replace(lb.gardenlocation, '^.*\)''(.*)''$', '\1')
         END AS gardenlocation,

         CASE
            WHEN (lb.gardenlocation is not null
                  AND lb.gardenlocation <> '')
            THEN 'Garden No. '||co1.objectnumber||', Bed '||regexp_replace(lb.gardenlocation, '^.*\)''(.*)''$', '\1')
            ELSE 'Garden No. '||co1.objectnumber||', Bed unknown'
         END AS gardeninfo,

         CASE
            WHEN lb.hortwild='Horticultural' THEN 'Horticultural voucher:'
            WHEN lb.hortwild='Wild' THEN 'Wild voucher:'
         END AS vouchertype,

         lb.fieldcollectionnote,
         lb.annotation,

         CASE
            WHEN (lbc.item is not null AND lbc.item <> ''
                  AND lc.loanoutdate is not null)
            THEN regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')||', '||to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')

            WHEN (lbc.item is not null
                  AND lbc.item <> ''
                  AND lc.loanoutdate is null)
            THEN regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')

            WHEN (lbc.item is null
                  AND lc.loanoutdate is not null)
            THEN to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')
         END AS vouchercollectioninfo

         FROM loansout_common lc
            JOIN loansout_naturalhistory lnh ON (lc.id=lnh.id)
            JOIN loansout_botgarden lb ON (lc.id=lb.id)
            LEFT OUTER JOIN loansout_botgarden_collectorlist lbc ON (lbc.id = lc.id AND lbc.pos=0)
            JOIN hierarchy h1 ON lc.id=h1.id
            JOIN relations_common r1 ON (h1.name=r1.subjectcsid AND objectdocumenttype='CollectionObject')
            JOIN hierarchy h2 ON (r1.objectcsid=h2.name)
            JOIN collectionobjects_common co1 ON (co1.id=h2.id)

            LEFT OUTER JOIN hierarchy htig
               ON (co1.id = htig.parentid
                   AND htig.pos = 0
                   AND htig.name = 'collectionobjects_naturalhistory:taxonomicIdentGroupList')
            LEFT OUTER JOIN taxonomicIdentGroup tig ON (tig.id = htig.id)

            JOIN collectionspace_core core ON (core.id=co1.id)
            JOIN misc misc2 ON (misc2.id = co1.id
                                AND misc2.lifecyclestate <> 'deleted')

            LEFT OUTER JOIN taxon_common tc ON (tig.taxon=tc.refname)
            LEFT OUTER JOIN taxon_naturalhistory tn ON (tc.id=tn.id)
            LEFT OUTER JOIN hierarchy htt
               ON (tc.id=htt.parentid
                   AND htt.name='taxon_common:taxonTermGroupList'
                   AND htt.pos=0) -- for now assuming preferred name
            LEFT OUTER JOIN taxontermgroup tt ON (tt.id=htt.id)
            LEFT OUTER JOIN collectionobjects_common_fieldCollectors fc ON (co1.id = fc.id
                                                                            AND fc.pos = 0)

            LEFT OUTER JOIN hierarchy hfcdg ON (co1.id = hfcdg.parentid
                                                AND hfcdg.name='collectionobjects_common:fieldCollectionDateGroup')
            LEFT OUTER JOIN structureddategroup sdg ON (sdg.id = hfcdg.id)

            LEFT OUTER JOIN hierarchy hlg ON (co1.id = hlg.parentid
                                              AND hlg.pos = 0
                                              AND hlg.name='collectionobjects_naturalhistory:localityGroupList')
            LEFT OUTER JOIN localitygroup lg ON (lg.id = hlg.id)

         WHERE lb.labelrequested = 'Yes'
         ORDER BY objectnumber

         LOOP
            -- return next r;

            sheetcount := r.numbersheets;

            FOR n IN 1..sheetcount LOOP
               RETURN next r;
            END LOOP;
         END LOOP;
      RETURN;

      END;
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE RETURNS NULL ON NULL INPUT;
      GRANT EXECUTE ON FUNCTION findvoucherlabels() to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'While creating function findvoucherlabels: missing relation';
      WHEN undefined_object THEN
         RAISE NOTICE 'While creating function findvoucherlabels: undefined object (probably voucherlabeltype)';
      WHEN OTHERS THEN
         RAISE NOTICE 'ERROR creating function findvoucherlabels: (%)', SQLSTATE;
   END;
END$DOS$
