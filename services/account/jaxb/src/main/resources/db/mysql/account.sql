alter table ACCOUNTLISTITEM drop foreign key FKBD8755BE37E86A94;
drop table if exists ACCOUNTLISTITEM;
drop table if exists ACCOUNTSCOMMON;
drop table if exists ACCOUNTSCOMMONLIST;
create table ACCOUNTLISTITEM (HJID bigint not null auto_increment, ANCHORNAME varchar(255), CSID varchar(255), EMAIL varchar(255), FIRSTNAME varchar(255), LASTNAME varchar(255), MI varchar(255), URI varchar(255), ACCOUNTLISTITEM_ACCOUNTSCOMM_0 bigint, primary key (HJID));
create table ACCOUNTSCOMMON (CSID varchar(255) not null, ANCHORNAME varchar(255), EMAIL varchar(255), FIRSTNAME varchar(255), LASTNAME varchar(255), MI varchar(255), PHONE varchar(255), primary key (CSID));
create table ACCOUNTSCOMMONLIST (HJID bigint not null auto_increment, primary key (HJID));
alter table ACCOUNTLISTITEM add index FKBD8755BE37E86A94 (ACCOUNTLISTITEM_ACCOUNTSCOMM_0), add constraint FKBD8755BE37E86A94 foreign key (ACCOUNTLISTITEM_ACCOUNTSCOMM_0) references ACCOUNTSCOMMONLIST (HJID);
