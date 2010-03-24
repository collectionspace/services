drop table if exists roles;
drop table if exists users_roles;
create table roles (rolename varchar(200) not null, created_at datetime not null, rolegroup varchar(255) not null, updated_at datetime, primary key (rolename));
create table users_roles (HJID bigint not null auto_increment, created_at datetime not null, rolename varchar(200) not null, updated_at datetime, username varchar(128) not null, primary key (HJID), unique (username, rolename));
