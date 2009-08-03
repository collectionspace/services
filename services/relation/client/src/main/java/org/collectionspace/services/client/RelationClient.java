package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

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
public class RelationClient extends BaseServiceClient {

    /**
     *
     */
    private static final RelationClient instance = new RelationClient();
    /**
     *
     */
    private RelationProxy relationProxy;

    /**
     *
     * Default constructor for RelationClient class.
     *
     */
    private RelationClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        if(useAuth()){
        	relationProxy = ProxyFactory.create(RelationProxy.class,
                    getBaseURL(), getHttpClient());
        }else{
        	relationProxy = ProxyFactory.create(RelationProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static RelationClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#getRelation()
     */
    public ClientResponse<RelationList> getRelationList() {
        return relationProxy.getRelationList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#getRelation(java.lang.String)
     */
    public ClientResponse<Relation> getRelation(String csid) {
        return relationProxy.getRelation(csid);
    }

    /**
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#createRelation(org.collectionspace.hello.Relation)
     */
    public ClientResponse<Response> createRelation(Relation relation) {
        return relationProxy.createRelation(relation);
    }

    /**
     * @param csid
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#updateRelation(java.lang.Long, org.collectionspace.hello.Relation)
     */
    public ClientResponse<Relation> updateRelation(String csid, Relation relation) {
        return relationProxy.updateRelation(csid, relation);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#deleteRelation(java.lang.Long)
     */
    public ClientResponse<Response> deleteRelation(String csid) {
        return relationProxy.deleteRelation(csid);
    }
}
