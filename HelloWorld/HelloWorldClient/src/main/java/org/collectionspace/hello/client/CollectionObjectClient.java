package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.CollectionObject;
import org.collectionspace.hello.CollectionObjectList;

import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A CollectionObjectClient.

 * @version $Revision:$
 */
public class CollectionObjectClient {

	private static final String HOST = "http://localhost:8080";
	private static final String URI = "/helloworld/cspace-nuxeo";

    /**
     *
     */
    private static final CollectionObjectClient instance = new CollectionObjectClient();
    /**
     *
     */
    private CollectionObjectProxy collectionObjectProxy;

    /**
     *
     * Default constructor for CollectionObjectClient class.
     *
     */
    private CollectionObjectClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        collectionObjectProxy = ProxyFactory.create(CollectionObjectProxy.class, HOST + URI);
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static CollectionObjectClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#getCollectionObject()
     */
    public ClientResponse<CollectionObjectList> getCollectionObjectList() {
        return collectionObjectProxy.getCollectionObjectList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#getCollectionObject(java.lang.String)
     */
    public ClientResponse<CollectionObject> getCollectionObject(String csid) {
        return collectionObjectProxy.getCollectionObject(csid);
    }

    /**
     * @param collectionobject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#createCollectionObject(org.collectionspace.hello.CollectionObject)
     */
    public ClientResponse<Response> createCollectionObject(CollectionObject collectionObject) {
        return collectionObjectProxy.createCollectionObject(collectionObject);
    }

    /**
     * @param csid
     * @param collectionobject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#updateCollectionObject(java.lang.Long, org.collectionspace.hello.CollectionObject)
     */
    public ClientResponse<CollectionObject> updateCollectionObject(String csid, CollectionObject collectionObject) {
        return collectionObjectProxy.updateCollectionObject(csid, collectionObject);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#deleteCollectionObject(java.lang.Long)
     */
    public ClientResponse<Response> deleteCollectionObject(String csid) {
        return collectionObjectProxy.deleteCollectionObject(csid);
    }
}
