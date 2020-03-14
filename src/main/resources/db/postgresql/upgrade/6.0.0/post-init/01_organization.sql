-- Upgrade organization. Move contact names into the new repeating contact group (DRYD-566).

INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
  SELECT gen_random_uuid(), id, pos, 'organizations_common:contactGroupList', TRUE, 'contactGroup'
    FROM organizations_common_contactnames;

INSERT INTO contactgroup (id, contactname)
  SELECT h.id, occ.item
    FROM organizations_common_contactnames occ
    INNER JOIN hierarchy h ON h.parentid = occ.id AND h.pos = occ.pos AND h.name = 'organizations_common:contactGroupList';
