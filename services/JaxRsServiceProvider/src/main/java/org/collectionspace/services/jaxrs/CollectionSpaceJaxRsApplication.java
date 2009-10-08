package org.collectionspace.services.jaxrs;

import org.collectionspace.services.collectionobject.CollectionObjectResource;
import org.collectionspace.services.id.IDResource;
import org.collectionspace.services.intake.IntakeResource;
////import org.collectionspace.services.relation.RelationResource;
import org.collectionspace.services.relation.NewRelationResource;
import org.collectionspace.services.acquisition.AcquisitionResource;

import org.collectionspace.services.vocabulary.VocabularyResource;

//import org.collectionspace.services.query.QueryResource;

import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;


public class CollectionSpaceJaxRsApplication extends Application {

    private Set<Object> singletons = new HashSet<Object>();
    private Set<Class<?>> empty = new HashSet<Class<?>>();

    public CollectionSpaceJaxRsApplication() {
        singletons.add(new CollectionObjectResource());
        singletons.add(new IDResource());
        singletons.add(new IntakeResource());
        singletons.add(new AcquisitionResource());
//        singletons.add(new RelationResource());
        singletons.add(new NewRelationResource());

        singletons.add(new VocabularyResource());

//        singletons.add(new QueryResource());
//        singletons.add(new DomainIdentifierResource());
//        singletons.add(new PingResource());
    }

    @Override
    public Set<Class<?>> getClasses() {
        return empty;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }
}

