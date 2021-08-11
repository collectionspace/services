/* NAGPRA Report Filed Data Migration to Version 7.0
-- NAGPRA Report Filed field group changed to repeating group.
-- Migrates existing data to the new nagpraReportFiledGroup repeating group table.
   1) "NAGPRA report filed": collectionobjects_nagpra.nagprareportfiled 
         Copy to nagprareportfiledgroup.nagprareportfiled as a new record.
         Create new hierarchy record for primarytype = 'nagpraReportFiledGroup'
   2) "NAGPRA report filed by": collectionobjects_nagpra.nagprareportfiledby
         Copy to nagprareportfiledgroup.nagprareportfiledby as a new record.
         Create new hierarchy record for primarytype = 'nagpraReportFiledGroup' [same record as for 1)]
   3) "NAGPRA report filed date": structuredDateGroup.*
       join structuredDateGroup s, hierarchy h, collectionobjects_nagpra c
       on s.id = h.id and h.parentid = c.id and h.name = 'collectionobjects_nagpra:nagpraReportFiledDate'
         Copy to structureddategroup.* as a new record.
         Create new hierarchy record for primarytype = 'structuredDateGroup', name = 'nagpraReportFiledDate'
-- Notes:
--    Requires uuid_generate_v4() function for generating UUID
--    Since this script may possibly be run repeatedly, it checks for data in the new tables
         and it only creates a new record if a matching record does not exist.
--    Once existing data in collectionobjects_nagpra has been migrated, this script does not delete defunct data.
*/

-- BEGIN MIGRATION

DO $$

DECLARE

   trow record;
   newpos int;
   rc int;

BEGIN

   -- If public.collectionobjects_nagpra does not exist or contains 0 records, there is nothing to migrate.
   IF (select to_regclass('collectionobjects_nagpra')::text = 'collectionobjects_nagpra') AND (SELECT count(*) from collectionobjects_nagpra) > 0 THEN

      DROP TABLE IF EXISTS temp_cn;
   
      CREATE TEMP TABLE temp_cn AS
      SELECT
         uuid_generate_v4()::varchar AS new_nrfgid,
         cn.id as cnid,
         cn.nagprareportfiled,
         cn.nagprareportfiledby,
         uuid_generate_v4()::varchar AS new_sdgid,
         sdg.*
      FROM collectionobjects_nagpra cn
      LEFT OUTER JOIN hierarchy hsdg ON (cn.id = hsdg.parentid and hsdg.name = 'collectionobjects_nagpra:nagpraReportFiledDate')
      LEFT OUTER JOIN structureddategroup sdg ON (hsdg.id = sdg.id)
      ORDER BY cnid;
   
      DROP TABLE IF EXISTS temp_nrfg;
   
      CREATE TEMP TABLE temp_nrfg AS
      SELECT
         hnrfg.parentid as nrfg_parentid,
         nrfg.nagprareportfiled,
         nrfg.nagprareportfiledby,
         sdg.datedisplaydate
      FROM nagprareportfiledgroup nrfg
      LEFT OUTER JOIN hierarchy hnrfg ON (nrfg.id = hnrfg.id)
      LEFT OUTER JOIN hierarchy hsdg ON (nrfg.id = hsdg.parentid AND hsdg.name = 'nagpraReportFiledDate')
      LEFT OUTER JOIN structureddategroup sdg ON (hsdg.id = sdg.id);

      -- Check for existing nagpraReportFiledGroup records that match collectionobjects_nagpra records.
      PERFORM cn.cnid
      FROM temp_cn cn
      LEFT OUTER JOIN temp_nrfg nrfg ON (cn.cnid = nrfg.nrfg_parentid)
      WHERE
         coalesce(cn.nagprareportfiled::text, '') || '|' || coalesce(cn.nagprareportfiledby, '')
            || '|' || coalesce(cn.datedisplaydate, '') !=
         coalesce(nrfg.nagprareportfiled::text, '') || '|' || coalesce(nrfg.nagprareportfiledby, '')
            || '|' || coalesce(nrfg.datedisplaydate, '')
      AND cn.new_nrfgid NOT IN (
         SELECT c.new_nrfgid FROM temp_cn c JOIN temp_nrfg n ON (c.cnid = n.nrfg_parentid)
         WHERE
            coalesce(c.nagprareportfiled::text, '') || '|' || coalesce(c.nagprareportfiledby, '')
               || '|' || coalesce(c.datedisplaydate, '') =
            coalesce(n.nagprareportfiled::text, '') || '|' || coalesce(n.nagprareportfiledby, '')
               || '|' || coalesce(n.datedisplaydate, ''));

      GET DIAGNOSTICS rc = ROW_COUNT;

      IF rc = 0 THEN

        RAISE NOTICE 'INFO: Nothing to migrate.';

      ELSE

        RAISE NOTICE 'INFO: % NAGPRA Report Filed records to migrate.', rc;

         FOR trow IN
            SELECT cn.*
            FROM temp_cn cn
            LEFT OUTER JOIN temp_nrfg nrfg ON (cn.cnid = nrfg.nrfg_parentid)
            WHERE
               coalesce(cn.nagprareportfiled::text, '') || '|' || coalesce(cn.nagprareportfiledby, '')
                  || '|' || coalesce(cn.datedisplaydate, '') !=
               coalesce(nrfg.nagprareportfiled::text, '') || '|' || coalesce(nrfg.nagprareportfiledby, '')
                  || '|' || coalesce(nrfg.datedisplaydate, '')
            AND cn.new_nrfgid NOT IN (
               SELECT c.new_nrfgid FROM temp_cn c JOIN temp_nrfg n ON (c.cnid = n.nrfg_parentid)
               WHERE
                  coalesce(c.nagprareportfiled::text, '') || '|' || coalesce(c.nagprareportfiledby, '')
                     || '|' || coalesce(c.datedisplaydate, '') =
                  coalesce(n.nagprareportfiled::text, '') || '|' || coalesce(n.nagprareportfiledby, '')
                     || '|' || coalesce(n.datedisplaydate, ''))
   
         LOOP
   
            -- Check for duplicates in collectionobjects_nagpra
            IF (SELECT count(*) FROM temp_cn where cnid = trow.cnid) > 1 THEN
   
               RAISE NOTICE 'ERROR: More than 1 collectionobjects_nagpra record found for id = "%".', trow.cnid;
   
            ELSE -- Migrate NAGPRA Report Filed data:
   
               RAISE NOTICE 'INFO: MIGRATING NAGPRA Report Filed data for CollObjID = "%"', trow.cnid;
   
               -- Get max pos value for nagpraReportFiledGroup records:
               SELECT coalesce(max(pos), -1) + 1 INTO newpos
               FROM hierarchy
               WHERE parentid = trow.cnid
               AND primarytype = 'nagpraReportFiledGroup';
   
               -- Create new nagpraReportFiledGroup hierarchy record for collectionobjects_nagpra record
               INSERT INTO hierarchy (
                  id,
                  parentid,
                  pos,
                  name,
                  isproperty,
                  primarytype)
               VALUES (
                  trow.new_nrfgid,
                  trow.cnid,
                  newpos,
                  'collectionobjects_nagpra:nagpraReportFiledGroupList',
                  True,
                  'nagpraReportFiledGroup');
   
               GET DIAGNOSTICS rc = ROW_COUNT;
               RAISE NOTICE '    : INSERTED % into hierarchy, id = "%", pos %, nagpraReportFiledGroup.', rc, trow.new_nrfgid, newpos;
   
               -- Create new nagpraReportFiledGroup record from collectionobjects_nagpra record
               INSERT INTO nagprareportfiledgroup (
                  id,
                  nagprareportfiled,
                  nagprareportfiledby)
               VALUES (
                  trow.new_nrfgid,
                  trow.nagprareportfiled,
                  trow.nagprareportfiledby);
   
               -- Create new nagpraReportFiledDate hierarchy record for collectionobjects_nagpra record
               INSERT INTO hierarchy (
                  id,
                  parentid,
                  name,
                  isproperty,
                  primarytype)
               VALUES (
                  trow.new_sdgid,
                  trow.new_nrfgid,
                  'nagpraReportFiledDate',
                  True,
                  'structuredDateGroup');
   
               GET DIAGNOSTICS rc = ROW_COUNT;
               RAISE NOTICE '    : INSERTED % into hierarchy, id = "%", nagpraReportFiledDate.', rc, trow.new_sdgid;
   
               -- Migrate/Create new structuredDateGroup record from collectionobjects_nagpra record
               IF trow.id IS NOT NULL THEN
                  INSERT INTO structureddategroup (
                     id,
                     dateearliestsinglequalifier,
                     scalarvaluescomputed,
                     datelatestyear,
                     datelatestday,
                     dateassociation,
                     dateearliestsingleera,
                     datedisplaydate,
                     dateearliestsinglecertainty,
                     datelatestera,
                     dateearliestsinglequalifiervalue,
                     datelatestcertainty,
                     dateearliestsingleyear,
                     datelatestqualifier,
                     datelatestqualifiervalue,
                     dateearliestsinglequalifierunit,
                     dateperiod,
                     dateearliestscalarvalue,
                     datelatestmonth,
                     datenote,
                     datelatestscalarvalue,
                     datelatestqualifierunit,
                     dateearliestsingleday,
                     dateearliestsinglemonth)
                  VALUES (
                     trow.new_sdgid,
                     trow.dateearliestsinglequalifier,
                     trow.scalarvaluescomputed,
                     trow.datelatestyear,
                     trow.datelatestday,
                     trow.dateassociation,
                     trow.dateearliestsingleera,
                     trow.datedisplaydate,
                     trow.dateearliestsinglecertainty,
                     trow.datelatestera,
                     trow.dateearliestsinglequalifiervalue,
                     trow.datelatestcertainty,
                     trow.dateearliestsingleyear,
                     trow.datelatestqualifier,
                     trow.datelatestqualifiervalue,
                     trow.dateearliestsinglequalifierunit,
                     trow.dateperiod,
                     trow.dateearliestscalarvalue,
                     trow.datelatestmonth,
                     trow.datenote,
                     trow.datelatestscalarvalue,
                     trow.datelatestqualifierunit,
                     trow.dateearliestsingleday,
                     trow.dateearliestsinglemonth);
   
                  GET DIAGNOSTICS rc = ROW_COUNT;
                  RAISE NOTICE '    : INSERTED % into structuredDatetGroup, id = "%".', rc, trow.new_sdgid;
      
               END IF;
   
            END IF;
   
         END LOOP;

      END IF;

   ELSE

      RAISE NOTICE 'collectionobjects_nagpra does not exist or has 0 records';

   END IF;

END $$;

-- END MIGRATION
