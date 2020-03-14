-- Upgrade intake. Move depositor and currentowner into repeating fields (DRYD-801).

INSERT INTO intakes_common_currentowners (id, pos, item)
  SELECT id, 0, currentowner
    FROM intakes_common
    WHERE currentowner IS NOT NULL;

INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
  SELECT gen_random_uuid(), id, 0, 'intakes_common:depositorGroupList', TRUE, 'depositorGroup'
    FROM intakes_common
    WHERE depositor IS NOT NULL OR depositorsrequirements IS NOT NULL;

INSERT INTO depositorgroup (id, depositor, depositorsrequirements)
  SELECT h.id, ic.depositor, ic.depositorsrequirements
    FROM intakes_common ic
    INNER JOIN hierarchy h ON h.parentid = ic.id AND h.name = 'intakes_common:depositorGroupList'
    WHERE ic.depositor IS NOT NULL OR ic.depositorsrequirements IS NOT NULL;
