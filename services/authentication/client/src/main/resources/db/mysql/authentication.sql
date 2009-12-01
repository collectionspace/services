drop table if exists roles;
drop table if exists users;
drop table if exists users_roles;
create table roles (rolename varchar(255) not null, rolegroup varchar(255) not null, tenantid varchar(255) not null, primary key (rolename));
create table users (username varchar(255) not null, passwd varchar(128) not null, tenantid varchar(255) not null, primary key (username));
create table users_roles (HJID bigint not null auto_increment, rolename varchar(255) not null, tenantid varchar(255) not null, username varchar(255) not null, primary key (HJID));
