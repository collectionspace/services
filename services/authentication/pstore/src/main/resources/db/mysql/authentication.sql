drop table if exists users;
create table users (username varchar(128) not null, created_at datetime not null, passwd varchar(128) not null, updated_at datetime, primary key (username));
