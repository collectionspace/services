-- Set vocabulary csids to known values. This script should be run after recreating a database and initializing authorities.

-- Citation
update hierarchy set name='0fada64d-eb2c-442d-b951' where id = (select common.id from citationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='citation');

-- Concept
update hierarchy set name='e80e2b37-5e34-475a-b9d8' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='concept');
update hierarchy set name='74571044-8b19-4d55-970d' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='genre');
update hierarchy set name='46cf0ea2-adfa-4694-8687' where id = (select common.id from conceptauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='theme');

-- Location
update hierarchy set name='84a9a735-27f1-4b74-9955' where id = (select common.id from locationauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='location');

-- Organization
update hierarchy set name='79c7ae8c-c4dc-46b4-ad00' where id = (select common.id from orgauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='committee');
update hierarchy set name='5a1317ec-6246-4737-9d92' where id = (select common.id from orgauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='organization');

-- Person
update hierarchy set name='ae43b316-d4ff-475f-921c' where id = (select common.id from personauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='person');

-- Place
update hierarchy set name='bba3b2f9-ca14-4d5c-821c' where id = (select common.id from placeauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='place');

-- Work
update hierarchy set name='d28dfc96-da03-4b0b-9ae1' where id = (select common.id from workauthorities_common common inner join collectionspace_core cc on common.id=cc.id where cc.tenantid=50 and shortidentifier='work');
