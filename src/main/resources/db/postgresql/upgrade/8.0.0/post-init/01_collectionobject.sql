-- Upgrade collectionobject. Move fieldCollectionPlace into repeating field (DRYD-1395).
DO $$
DECLARE
    trow record;
    maxpos int;
BEGIN
    -- For new install, if collectionobjects_common.fieldcollectionplace does not exist, there is nothing to migrate.
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name='collectionobjects_common' AND column_name='fieldcollectionplace') THEN
        FOR trow IN
            -- Get record in collectionobjects_common that does not have an existing/matching record in
            -- collectionobjects_common_fieldcollectionplaces:
            SELECT co.id, co.fieldcollectionplace
            FROM public.collectionobjects_common co
            LEFT OUTER JOIN public.collectionobjects_common_fieldcollectionplaces co_fcp ON
                (co.id = co_fcp.id AND co.fieldcollectionplace = co_fcp.item)
            WHERE co.fieldcollectionplace IS NOT NULL AND co.fieldcollectionplace != '' AND co_fcp.item IS NULL

            LOOP
                -- Get max pos value for the collectionobject record's field collection place field:
                SELECT coalesce(max(pos), -1) INTO maxpos
                FROM public.collectionobjects_common_fieldcollectionplaces
                WHERE id = trow.id;

                -- Migrate collectionobjects_common fieldcollectionplace data to
                -- collectionobjects_common_fieldcollectionplaces table:
                INSERT INTO public.collectionobjects_common_fieldcollectionplaces(id, pos, item)
                VALUES (trow.id, maxpos + 1, trow.fieldcollectionplace);
            END LOOP;
    ELSE
        RAISE NOTICE 'No v7.2 collectionobject field collection place data to migrate: collectionobjects_common.fieldcollectionplace does not exist';
    END IF;
END
$$;
