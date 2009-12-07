drop table if exists accounts_common;
create table accounts_common (csid varchar(255) not null, email longtext not null, first_name longtext not null, last_name longtext not null, mi varchar(1), mobile varchar(255), phone varchar(255), screen_name varchar(128) not null, status varchar(15) not null, tenantid varchar(255) not null, userid longtext not null, primary key (csid));
