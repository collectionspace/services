package org.collectionspace.services.client;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A RelationClient.

 * @version $Revision:$
 */
public class RelationClient extends BaseServiceClient implements RelationProxy {

    /**
     *
     */
    private RelationProxy relationProxy;

    /**
    *
    * Default constructor for CollectionObjectClient class.
    *
    */
   public RelationClient() {
       ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
       RegisterBuiltin.register(factory);
       setProxy();
   }
    
   /**
    * allow to reset proxy as per security needs
    */
   public void setProxy() {
       if(useAuth()){
           relationProxy = ProxyFactory.create(RelationProxy.class,
                   getBaseURL(), getHttpClient());
       }else{
    	   relationProxy = ProxyFactory.create(RelationProxy.class,
                   getBaseURL());
       }
   }

    /**
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#getRelation()
     */
    public ClientResponse<RelationList> readList() {
        return relationProxy.readList();
    }
    
    public ClientResponse<RelationList> readList_SPO(String subjectCsid,
			String predicate,
			String objectCsid) {
    	return relationProxy.readList_SPO(subjectCsid, predicate, objectCsid);
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#getRelation(java.lang.String)
     */
    public ClientResponse<Relation> read(String csid) {
        return relationProxy.read(csid);
    }

    /**
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#createRelation(org.collectionspace.hello.Relation)
     */
    public ClientResponse<Response> create(Relation relation) {
        return relationProxy.create(relation);
    }

    /**
     * @param csid
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#updateRelation(java.lang.Long, org.collectionspace.hello.Relation)
     */
    public ClientResponse<Relation> update(String csid, Relation relation) {
        return relationProxy.update(csid, relation);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#deleteRelation(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return relationProxy.delete(csid);
    }
}
