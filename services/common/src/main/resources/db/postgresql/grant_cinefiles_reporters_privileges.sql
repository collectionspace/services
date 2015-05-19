DO $$
BEGIN
   IF EXISTS (
      SELECT *
      FROM   pg_catalog.pg_group
      WHERE  groname = 'reporters_cinefiles') THEN

      GRANT SELECT ON ALL TABLES IN SCHEMA public TO GROUP reporters_cinefiles;
   END IF;
END $$;
