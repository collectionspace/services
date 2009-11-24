drop table if exists accounts_common;
create table accounts_common (csid varchar(255) not null, anchor_name varchar(128) not null, email longtext not null, first_name longtext not null, last_name longtext not null, mi varchar(1), mobile varchar(15), phone varchar(15), primary key (csid));
