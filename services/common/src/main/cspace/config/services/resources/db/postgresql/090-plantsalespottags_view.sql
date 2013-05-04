
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

