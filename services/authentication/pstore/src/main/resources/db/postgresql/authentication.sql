DROP TABLE IF EXISTS users;
create table users (username varchar(128) not null, created_at timestamp not null, passwd varchar(128) not null, updated_at timestamp, primary key (username));
