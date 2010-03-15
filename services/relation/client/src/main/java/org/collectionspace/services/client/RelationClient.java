package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

//import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.relation.RelationsCommonList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A RelationClient.

 * @version $Revision:$
 */
public class RelationClient extends AbstractServiceClientImpl implements RelationProxy {

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	public String getServicePathComponent() {
		return "relations";
	}
	
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
    public ClientResponse<RelationsCommonList> readList() {
        return relationProxy.readList();
    }
    
    public ClientResponse<RelationsCommonList> readList_SPO(String subjectCsid,
			String predicate,
			String objectCsid) {
    	return relationProxy.readList_SPO(subjectCsid, predicate, objectCsid);
    }
    
    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#getRelation(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return relationProxy.read(csid);
    }

    /**
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#createRelation(org.collectionspace.hello.Relation)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return relationProxy.create(multipart);
    }

    /**
     * @param csid
     * @param relation
     * @return
     * @see org.collectionspace.hello.client.RelationProxy#updateRelation(java.lang.Long, org.collectionspace.hello.Relation)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return relationProxy.update(csid, multipart);
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
