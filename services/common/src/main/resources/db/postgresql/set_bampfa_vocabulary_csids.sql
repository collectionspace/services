-- Set vocabulary csids to known values. This script should be run after recreating a database and initializing authorities.

-- Citation
update hierarchy set name='ae252a90-4490-4915-b8c4' where id = (select common.id from citationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='citation');
update hierarchy set name='533efc65-3ba6-4086-9a88' where id = (select common.id from citationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='worldcat');

-- Concept
update hierarchy set name='b6080a2e-43f8-4b79-96d7' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='activity');
update hierarchy set name='51d6d3d1-160d-48ee-99bb' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='concept');
update hierarchy set name='189d3168-0619-43fa-94b4' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='material_ca');

-- Location
update hierarchy set name='50257c73-875c-434e-81a7' where id = (select common.id from locationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='crate');
update hierarchy set name='e2b86b91-0f93-48ed-820c' where id = (select common.id from locationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='location');
update hierarchy set name='01d6aa09-181e-46d8-81fe' where id = (select common.id from locationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='offsite_sla');

-- Organization
update hierarchy set name='5dd949bb-1a02-4d85-bc1b' where id = (select common.id from orgauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='collective');
update hierarchy set name='df40282d-2bd6-4711-a3f6' where id = (select common.id from orgauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='organization');
update hierarchy set name='f4a1b06c-5872-42b5-95d8' where id = (select common.id from orgauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='ulan_oa');

-- Person
update hierarchy set name='1e3308ba-9d64-49e7-9541' where id = (select common.id from personauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='person');
update hierarchy set name='d62430b8-16f6-4ef5-ac36' where id = (select common.id from personauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='ulan_pa');

-- Place
update hierarchy set name='965d0e1c-0754-4953-b24e' where id = (select common.id from placeauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='place');
update hierarchy set name='ea4d6bb7-d7a2-40c6-a045' where id = (select common.id from placeauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='tgn_place');

-- Work
update hierarchy set name='8a0f7500-3cea-497f-89c2' where id = (select common.id from workauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='cona_work');
update hierarchy set name='8d8af2c3-5461-4116-8ff6' where id = (select common.id from workauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=55 and shortidentifier='work');