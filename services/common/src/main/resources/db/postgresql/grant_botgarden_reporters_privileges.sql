DO $$
BEGIN
   IF EXISTS (
      SELECT *
      FROM   pg_catalog.pg_group
      WHERE  groname = 'reporters_botgarden') THEN

      GRANT SELECT ON ALL TABLES IN SCHEMA public TO GROUP reporters_botgarden;
   END IF;
END $$;
