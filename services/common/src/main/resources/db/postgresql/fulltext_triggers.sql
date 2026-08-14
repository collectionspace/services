-- Remove punctuation from fulltext columns
-- Note: There is a nuxeo trigger which sets the fulltext.fulltext column by coalescing the simpletext and binarytext.
CREATE OR REPLACE FUNCTION fulltext_normalize() RETURNS TRIGGER AS $$
BEGIN
  NEW.simpletext = regexp_replace(NEW.simpletext, '[^\w]+', ' ', 'gi');
  NEW.binarytext = regexp_replace(NEW.binarytext, '[^\w]+', ' ', 'gi');
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS fulltext_normalize_trigger ON fulltext;
CREATE TRIGGER fulltext_normalize_trigger
BEFORE INSERT OR UPDATE ON fulltext
FOR EACH ROW
EXECUTE FUNCTION fulltext_normalize();
