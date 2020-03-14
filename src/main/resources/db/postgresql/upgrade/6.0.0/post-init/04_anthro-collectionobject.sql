-- Upgrade the anthropology collectionobject extension. Move nagpra cultural determinations into
-- the the note field in the new nagpra determination group (DRYD-820).

INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
  SELECT gen_random_uuid(), id, pos, 'collectionobjects_nagpra:nagpraDetermGroupList', TRUE, 'nagpraDetermGroup'
    FROM collectionobjects_nagpra_nagpraculturaldeterminations;

INSERT INTO nagpradetermgroup (id, nagpradetermnote)
  SELECT h.id, cnn.item
    FROM collectionobjects_nagpra_nagpraculturaldeterminations cnn
    INNER JOIN hierarchy h ON h.parentid = cnn.id AND h.pos = cnn.pos AND h.name = 'collectionobjects_nagpra:nagpraDetermGroupList';
