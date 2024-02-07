/*
 * If the unaccent extension is installed, modify the cspace_english text search configuration to
 * be accent-insensitive.
 */

DO $$
BEGIN

	IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'unaccent') THEN
		IF EXISTS (SELECT 1 FROM pg_ts_config WHERE cfgname = 'cspace_english') THEN
			ALTER TEXT SEARCH CONFIGURATION cspace_english
				ALTER MAPPING FOR asciihword, asciiword, hword_asciipart, hword, hword_part, word
				WITH unaccent, english_stem;
		END IF;
	END IF;

END $$;
