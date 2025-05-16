-- Convert a URN (often refname) (DRYD-1748: Add deurn function to database)
CREATE OR REPLACE FUNCTION deurn(urnstr VARCHAR) RETURNS VARCHAR
AS $BODY$ BEGIN RETURN ((regexp_match(urnstr, '''([^''\\]*(\\.[^''\\]*)*)'''))[1]); END $BODY$ LANGUAGE plpgsql IMMUTABLE;
