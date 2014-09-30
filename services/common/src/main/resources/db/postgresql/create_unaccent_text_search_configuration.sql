DO $$
BEGIN

	IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'unaccent') THEN
		CREATE EXTENSION unaccent;
	END IF;
	
	IF NOT EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname = 'unaccent_english') THEN
		CREATE TEXT SEARCH CONFIGURATION unaccent_english ( COPY = english );

		ALTER TEXT SEARCH CONFIGURATION unaccent_english 
			ALTER MAPPING FOR asciihword, asciiword, hword_asciipart, hword, hword_part, word 
			WITH unaccent, english_stem;
	END IF;
		
END $$;