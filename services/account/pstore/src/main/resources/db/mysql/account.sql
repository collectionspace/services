alter table accounts_tenants drop foreign key FKFDA649B05A9CEEB5;
drop table if exists accounts_common;
drop table if exists accounts_tenants;
drop table if exists tenants;
create table accounts_common (csid varchar(128) not null, created_at datetime not null, email varchar(255) not null, metadata_protection varchar(255), mobile varchar(255), person_ref_name varchar(255), phone varchar(255), roles_protection varchar(255), screen_name varchar(128) not null, status varchar(15) not null, updated_at datetime, userid varchar(128) not null, primary key (csid));
create table accounts_tenants (HJID bigint not null auto_increment, tenant_id varchar(128) not null, TENANTS_ACCOUNTSCOMMON_CSID varchar(128), primary key (HJID));
create table tenants (id varchar(128) not null, created_at datetime not null, name varchar(255) not null, updated_at datetime, primary key (id));
alter table accounts_tenants add index FKFDA649B05A9CEEB5 (TENANTS_ACCOUNTSCOMMON_CSID), add constraint FKFDA649B05A9CEEB5 foreign key (TENANTS_ACCOUNTSCOMMON_CSID) references accounts_common (csid);
