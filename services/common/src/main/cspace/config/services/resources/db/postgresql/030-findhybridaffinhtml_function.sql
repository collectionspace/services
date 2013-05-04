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

