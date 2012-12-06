CREATE OR REPLACE FUNCTION lastidentifiedlocation(character varying) RETURNS character varying
    AS 'select m.currentlocation as lastidentifiedlocation
from movements_common m,
hierarchy h1, 
relations_common r, 
hierarchy h2,
collectionobjects_common c,
misc misc
where m.id=h1.id
and r.subjectcsid=h1.name 
and r.subjectdocumenttype=''Movement''
and r.objectdocumenttype=''CollectionObject''
and r.objectcsid=h2.name 
and h2.id=c.id
and misc.id = c.id
and misc.lifecyclestate <> ''deleted''
and m.currentlocation is not null
and m.locationdate is not null
and h2.name=$1
order by m.locationdate desc,row_number() over(order by locationdate)
limit 1'
LANGUAGE SQL
    IMMUTABLE
    RETURNS NULL ON NULL INPUT;
