
-- Create the findvoucherlabels function.
--   Catch and report 'undefined_table' exception for any referenced table that
--   does not exist.  Catch and report any other exceptions with an error code.

-- used by voucher label report to use number of sheets to print multiple voucher labels
-- CRH 2/2/2013

DO $DO$
BEGIN
   BEGIN
      CREATE OR REPLACE FUNCTION findvoucherlabels() RETURNS setof voucherlabeltype AS
      $CF$
      DECLARE
          sheetcount integer;
          r voucherlabeltype%rowtype;
          n integer;
      
      BEGIN
      
      FOR r IN
      select co1.objectnumber,
      findhybridaffinhtml(tig.id) determinationformatted,
      case when (tn.family is not null and tn.family <> '')
           then regexp_replace(tn.family, '^.*\)''(.*)''$', '\1')
      end as family,
      case when fc.item is not null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is not null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber||', '||sdg.datedisplaydate
        when fc.item is not null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' '||co1.fieldcollectionnumber||', s.d.'
        when fc.item is not null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is not null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' s.n., '||sdg.datedisplaydate
        when fc.item is not null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is null
              then regexp_replace(fc.item, '^.*\)''(.*)''$', '\1')||' s.n., s.d.'
        when fc.item is null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is not null
              then co1.fieldcollectionnumber||', '||sdg.datedisplaydate
        when fc.item is null and co1.fieldcollectionnumber is not null and sdg.datedisplaydate is null
              then co1.fieldcollectionnumber||', s.d.'
        when fc.item is null and co1.fieldcollectionnumber is null and sdg.datedisplaydate is not null
              then 's.n., '||sdg.datedisplaydate
      end as collectioninfo,
      lc.loanoutnumber vouchernumber,
      lnh.numlent numbersheets,
      lb.labelrequested,
      case when (lb.gardenlocation is not null and lb.gardenlocation <> '')
           then 'Garden No. '||co1.objectnumber||', Bed '||regexp_replace(lb.gardenlocation, '^.*\)''(.*)''$', '\1')
           else 'Garden No. '||co1.objectnumber||', Bed unknown'
      end as gardeninfo,
      case when lb.hortwild='Horticultural' then 'Horticultural voucher:'
           when lb.hortwild='Wild' then 'Wild voucher:'
      end as vouchertype,
      lb.fieldcollectionnote,
      lb.annotation,
      case when (lbc.item is not null and lbc.item <> '' and lc.loanoutdate is not null)
            then regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')||', '||to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')
           when (lbc.item is not null and lbc.item <> '' and lc.loanoutdate is null)
            then regexp_replace(lbc.item, '^.*\)''(.*)''$', '\1')
           when (lbc.item is null and lc.loanoutdate is not null)
            then to_char(date(lc.loanoutdate + interval '8 hours'), 'MM/dd/yyyy')
      end as vouchercollectioninfo
      from loansout_common lc
      join loansout_naturalhistory lnh on (lc.id=lnh.id)
      join loansout_botgarden lb on (lc.id=lb.id)
      left outer join loansout_botgarden_collectorlist lbc on (lbc.id = lc.id and lbc.pos=0)
      
      join hierarchy h1 on lc.id=h1.id
      join relations_common r1 on (h1.name=r1.subjectcsid and objectdocumenttype='CollectionObject')
      join hierarchy h2 on (r1.objectcsid=h2.name)
      join collectionobjects_common co1 on (co1.id=h2.id)
      
      left outer join hierarchy htig
           on (co1.id = htig.parentid and htig.pos = 0 and htig.name = 'collectionobjects_naturalhistory:taxonomicIdentGroupList')
      left outer join taxonomicIdentGroup tig on (tig.id = htig.id)
      
      join collectionspace_core core on (core.id=co1.id)
      join misc misc2 on (misc2.id = co1.id and misc2.lifecyclestate <> 'deleted')
      
      left outer join taxon_common tc on (tig.taxon=tc.refname)
      left outer join taxon_naturalhistory tn on (tc.id=tn.id)
      
      left outer join hierarchy htt
          on (tc.id=htt.parentid and htt.name='taxon_common:taxonTermGroupList' and htt.pos=0)
      left outer join taxontermgroup tt on (tt.id=htt.id)
      
      left outer join collectionobjects_common_fieldCollectors fc on (co1.id = fc.id and fc.pos = 0)
      
      left outer join hierarchy hfcdg on (co1.id = hfcdg.parentid  and hfcdg.name='collectionobjects_common:fieldCollectionDateGroup')
      left outer join structureddategroup sdg on (sdg.id = hfcdg.id)
      
      where lb.labelrequested = 'Yes'
      order by objectnumber
      
      LOOP
      
      -- return next r;
      
      sheetcount := r.numbersheets;
      
      for n in 1..sheetcount loop
      
      return next r;
      
      END LOOP;
      
      END LOOP;
      
      RETURN;
      END; 
      $CF$
      LANGUAGE 'plpgsql' IMMUTABLE
      RETURNS NULL ON NULL INPUT;
      ALTER FUNCTION findvoucherlabels() OWNER TO nuxeo;
      GRANT EXECUTE ON FUNCTION findvoucherlabels() to public;
   EXCEPTION
      WHEN undefined_table THEN
         RAISE NOTICE 'NOTICE: While creating function findvoucherlabels: missing relation';
      WHEN undefined_object THEN
         RAISE NOTICE 'NOTICE: While creating function findvoucherlabels: undefined object (probably voucherlabeltype)';
      WHEN OTHERS THEN
         RAISE 'ERROR: creating function findvoucherlabels: (%)', SQLSTATE;
   END;
END$DO$;

