--
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
         RETURN NULL;
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
