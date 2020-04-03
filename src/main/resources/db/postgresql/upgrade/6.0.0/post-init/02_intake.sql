-- Upgrade intake. Move depositor and currentowner into repeating fields (DRYD-801).

DO $$
DECLARE
    trow record;
    maxpos int;
BEGIN
    -- For new install, if intakes_common.currentowner does not exist, there is nothing to migrate.

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='intakes_common' AND column_name='currentowner') THEN
        FOR trow IN
            -- Get record in intakes_common that does not have an existing/matching record in intakes_common_currentowners:

            SELECT ic.id, ic.currentowner
            FROM public.intakes_common ic
            LEFT OUTER JOIN public.intakes_common_currentowners icc ON (ic.id = icc.id AND ic.currentowner = icc.item)
            WHERE ic.currentowner IS NOT NULL AND ic.currentowner != '' AND icc.item IS NULL

            LOOP
                -- Get max pos value for the intake record's current owner field:

                SELECT coalesce(max(pos), -1) INTO maxpos
                FROM public.intakes_common_currentowners
                WHERE id = trow.id;

                -- Migrate intakes_common current owner data to intakes_common_currentowners table:

                INSERT INTO public.intakes_common_currentowners (id, pos, item)
                VALUES (trow.id, maxpos + 1, trow.currentowner);

            END LOOP;

    ELSE
        RAISE NOTICE 'No v5.2 intake current owner data to migrate: intakes_common.currentowner does not exist';

    END IF;
END
$$;

-- INSERT INTO intakes_common_currentowners (id, pos, item)
--   SELECT id, 0, currentowner
--     FROM intakes_common
--     WHERE currentowner IS NOT NULL;

DO $$
DECLARE
    trow record;
    maxpos int;
    uuid varchar(36);
BEGIN

    -- For new install, if intakes_common.depositor does not exist, there is nothing to migrate.

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='intakes_common' AND column_name='depositor') THEN
        FOR trow IN
            -- Get record in intakes_common that does not have an existing/matching record in depositorgroup:
            -- Verify that there is data to migrate.

            SELECT id AS parentid, depositor, depositorsrequirements
            FROM public.intakes_common
            WHERE id NOT IN (
                SELECT ic.id
                FROM public.intakes_common ic
                JOIN public.hierarchy h ON (ic.id = h.parentid)
                JOIN public.depositorgroup dg ON (
                    h.id = dg.id
                    AND ic.depositor IS NOT DISTINCT FROM dg.depositor
                    AND ic.depositorsrequirements IS NOT DISTINCT FROM dg.depositorsrequirements
                )
            )
            AND (depositor IS NOT NULL OR depositorsrequirements IS NOT NULL)

            LOOP
                -- Get max pos value for the intake record's depositor group, and generate a new uuid:

                SELECT
                    coalesce(max(pos), -1),
                    uuid_generate_v4()::varchar
                INTO
                    maxpos,
                    uuid
                FROM public.hierarchy
                WHERE parentid = trow.parentid
                AND name = 'intakes_common:depositorGroupList'
                AND primarytype = 'depositorGroup';

                -- Insert new record into hierarchy table first, due to foreign key on depositorgroup table:

                INSERT INTO public.hierarchy (
                    id,
                    parentid,
                    pos,
                    name,
                    isproperty,
                    primarytype)
                VALUES (
                    uuid,
                    trow.parentid,
                    maxpos + 1,
                    'intakes_common:depositorGroupList',
                    TRUE,
                    'depositorGroup');

                -- Migrate intake depositor data into depositorgroup table:

                INSERT INTO public.depositorgroup (
                    id,
                    depositor,
                    depositorsrequirements)
                VALUES (
                    uuid,
                    trow.depositor,
                    trow.depositorsrequirements);

            END LOOP;

    ELSE
        RAISE NOTICE 'No v5.2 intake depositor data to migrate: intakes_common.depositor does not exist';

    END IF;
END
$$;

-- INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
--   SELECT uuid_generate_v4(), id, 0, 'intakes_common:depositorGroupList', TRUE, 'depositorGroup'
--     FROM intakes_common
--     WHERE depositor IS NOT NULL OR depositorsrequirements IS NOT NULL;

-- INSERT INTO depositorgroup (id, depositor, depositorsrequirements)
--   SELECT h.id, ic.depositor, ic.depositorsrequirements
--     FROM intakes_common ic
--     INNER JOIN hierarchy h ON h.parentid = ic.id AND h.name = 'intakes_common:depositorGroupList'
--     WHERE ic.depositor IS NOT NULL OR ic.depositorsrequirements IS NOT NULL;
