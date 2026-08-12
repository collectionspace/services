CREATE OR REPLACE FUNCTION fulltext_rm_punctuation() RETURNS TRIGGER AS $$
BEGIN
  NEW.simpletext = regexp_replace(NEW.simpletext, '[^\w]+', ' ', 'gi');
  NEW.binarytext = regexp_replace(NEW.binarytext, '[^\w]+', ' ', 'gi');
  RETURN NEW;
END
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS fulltext_rm_punct_trigger ON fulltext;
CREATE TRIGGER fulltext_rm_punct_trigger
BEFORE INSERT OR UPDATE ON fulltext
FOR EACH ROW
EXECUTE FUNCTION fulltext_rm_punctuation();
