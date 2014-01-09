-- DOCUMENT NUMBER

INSERT INTO id_generators
    (csid, displayname, description, priority, last_generated_id, id_generator_state)
  SELECT 
     '5abe7f10-cfeb-4c00-b6b5-bdf621692f0f',
     'Document Number',
     'Identifies CineFiles Documents.',
     '9',
     '',
'<org.collectionspace.services.id.SettableIDGenerator>
  <parts>
    <org.collectionspace.services.id.NumericIDGeneratorPart>
      <maxLength>6</maxLength>
      <initialValue>1</initialValue>
      <currentValue>-1</currentValue>
    </org.collectionspace.services.id.NumericIDGeneratorPart>
  </parts>
</org.collectionspace.services.id.SettableIDGenerator>'
  WHERE '5abe7f10-cfeb-4c00-b6b5-bdf621692f0f' NOT IN
        (
        SELECT  csid
        FROM    id_generators
        );