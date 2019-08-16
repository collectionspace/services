-- drop all the objects before dropping roles
DROP database IF EXISTS @DB_CSPACE_NAME@;

DROP user IF EXISTS @DB_CSPACE_USER@;
