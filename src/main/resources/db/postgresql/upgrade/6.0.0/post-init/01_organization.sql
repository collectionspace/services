-- Upgrade organization. Move contact names into the new repeating contact group (DRYD-566).

DO $$
DECLARE
    trow record;
    maxpos int;
    uuid varchar(36);
BEGIN

    -- For new install, if organizations_common_contactnames does not exist, there is nothing to migrate.

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='organizations_common_contactnames') THEN
        FOR trow IN
            -- Get record in organizations_common_contactnames that does not have an existing/matching record in contactgroup:
            -- Verify that there is data to migrate.

            SELECT id AS parentid, item, pos
            FROM public.organizations_common_contactnames
            WHERE ROW(id, pos) NOT IN (
                SELECT occ.id, occ.pos
                FROM public.organizations_common_contactnames occ
                JOIN public.hierarchy h ON (occ.id = h.parentid)
                JOIN public.contactgroup cg ON (
                    h.id = cg.id
                    AND occ.item IS NOT DISTINCT FROM cg.contactname
                )
            )
            AND (item IS NOT NULL)
            ORDER BY pos

            LOOP
                -- Get max pos value for the organization record's contact group, and generate a new uuid:

                SELECT
                    coalesce(max(pos), -1),
                    uuid_generate_v4()::varchar
                INTO
                    maxpos,
                    uuid
                FROM public.hierarchy
                WHERE parentid = trow.parentid
                    AND name = 'organizations_common:contactGroupList'
                    AND primarytype = 'contactGroup';

                -- Insert new record into hierarchy table first, due to foreign key on contactgroup table:

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
                    'organizations_common:contactGroupList',
                    TRUE,
                    'contactGroup');

                -- Migrate organization contact data into contactgroup table:

                INSERT INTO public.contactgroup (
                    id,
                    contactname)
                VALUES (
                    uuid,
                    trow.item);

            END LOOP;

    ELSE
        RAISE NOTICE 'No v5.2 organization data to migrate: organizations_common_contactnames does not exist';

    END IF;
END
$$;

-- INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
--   SELECT uuid_generate_v4(), id, pos, 'organizations_common:contactGroupList', TRUE, 'contactGroup'
--     FROM organizations_common_contactnames;

-- INSERT INTO contactgroup (id, contactname)
--   SELECT h.id, occ.item
--     FROM organizations_common_contactnames occ
--     INNER JOIN hierarchy h ON h.parentid = occ.id AND h.pos = occ.pos AND h.name = 'organizations_common:contactGroupList';
