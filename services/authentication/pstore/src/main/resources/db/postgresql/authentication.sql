DROP TABLE IF EXISTS users;
create table users (username varchar(128) not null, created_at timestamp not null, lastLogin timestamp, passwd varchar(128) not null, salt varchar(128), updated_at timestamp, primary key (username));

DROP TABLE IF EXISTS tokens;
create table tokens (id varchar(128) not null, account_csid varchar(128) not null, tenant_id varchar(128) not null, expire_seconds integer not null, enabled boolean not null, created_at timestamp not null, updated_at timestamp, primary key (id));
