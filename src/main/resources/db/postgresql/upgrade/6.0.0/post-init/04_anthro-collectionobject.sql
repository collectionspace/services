-- Upgrade the anthropology collectionobject extension. Move nagpra cultural determinations into
-- the the note field in the new nagpra determination group (DRYD-820).

DO $$
DECLARE
    trow record;
    maxpos int;
    uuid varchar(36);
BEGIN

    -- For new install, if collectionobjects_nagpra_nagpraculturaldeterminations does not exist, there is nothing to migrate.

    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='collectionobjects_nagpra_nagpraculturaldeterminations') THEN
        FOR trow IN
            -- Get record in collectionobjects_nagpra_nagpraculturaldeterminations that does not have an existing/matching record in nagpradetermgroup:
            -- Verify that there is data to migrate.

            SELECT id AS parentid, item, pos
            FROM public.collectionobjects_nagpra_nagpraculturaldeterminations
            WHERE ROW(id, pos) NOT IN (
                SELECT cnn.id, cnn.pos
                FROM public.collectionobjects_nagpra_nagpraculturaldeterminations cnn
                JOIN public.hierarchy h ON (cnn.id = h.parentid)
                JOIN public.nagpradetermgroup ndg ON (
                    h.id = ndg.id
                    AND cnn.item IS NOT DISTINCT FROM ndg.nagpradetermnote
                )
            )
            AND (item IS NOT NULL)
            ORDER BY pos

            LOOP
                -- Get max pos value for the collectionobject record's nagpra determination group, and generate a new uuid:

                SELECT
                    coalesce(max(pos), -1),
                    uuid_generate_v4()::varchar
                INTO
                    maxpos,
                    uuid
                FROM public.hierarchy
                WHERE parentid = trow.parentid
                    AND name = 'collectionobjects_nagpra:nagpraDetermGroupList'
                    AND primarytype = 'nagpraDetermGroup';

                -- Insert new record into hierarchy table first, due to foreign key on nagpradetermgroup table:

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
                    'collectionobjects_nagpra:nagpraDetermGroupList',
                    TRUE,
                    'nagpraDetermGroup');

                -- Migrate collectionobject nagpra cultural determination data into nagpradetermgroup table:

                INSERT INTO public.nagpradetermgroup (
                    id,
                    nagpradetermnote)
                VALUES (
                    uuid,
                    trow.item);

            END LOOP;

    ELSE
        RAISE NOTICE 'No v5.2 collectionobject nagpra cultural determination data to migrate: collectionobjects_nagpra_nagpraculturaldeterminations does not exist';

    END IF;
END
$$;

-- INSERT INTO hierarchy (id, parentid, pos, name, isproperty, primarytype)
--   SELECT uuid_generate_v4(), id, pos, 'collectionobjects_nagpra:nagpraDetermGroupList', TRUE, 'nagpraDetermGroup'
--     FROM collectionobjects_nagpra_nagpraculturaldeterminations;

-- INSERT INTO nagpradetermgroup (id, nagpradetermnote)
--   SELECT h.id, cnn.item
--     FROM collectionobjects_nagpra_nagpraculturaldeterminations cnn
--     INNER JOIN hierarchy h ON h.parentid = cnn.id AND h.pos = cnn.pos AND h.name = 'collectionobjects_nagpra:nagpraDetermGroupList';
