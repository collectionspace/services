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

    /**
     *
     */
    private static final CollectionObjectClient instance = new CollectionObjectClient();
    /**
     *
     */
    private CollectionObjectProxy CollectionObjectProxy;

    /**
     *
     * Create a new CollectionObjectClient.
     *
     */
    private CollectionObjectClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        CollectionObjectProxy =
          ProxyFactory.create(CollectionObjectProxy.class, "http://localhost:8080/helloworld/cspace");
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
     * @param id
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#getCollectionObject()
     */
    public ClientResponse<CollectionObjectList> getCollectionObjectList() {
        return CollectionObjectProxy.getCollectionObjectList();
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#getCollectionObject(java.lang.String)
     */
    public ClientResponse<CollectionObject> getCollectionObject(String id) {
        return CollectionObjectProxy.getCollectionObject(id);
    }

    /**
     * @param CollectionObject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#createCollectionObject(org.collectionspace.hello.client.entity.CollectionObject)
     */
    public ClientResponse<Response> createCollectionObject(CollectionObject CollectionObject) {
        return CollectionObjectProxy.createCollectionObject(CollectionObject);
    }

    /**
     * @param id
     * @param CollectionObject
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#updateCollectionObject(java.lang.String, org.collectionspace.hello.client.entity.CollectionObject)
     */
    public ClientResponse<CollectionObject> updateCollectionObject(String id, CollectionObject CollectionObject) {
        return CollectionObjectProxy.updateCollectionObject(id, CollectionObject);
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.CollectionObjectProxy#deleteCollectionObject(java.lang.String)
     */
    public ClientResponse<Response> deleteCollectionObject(String id) {
        return CollectionObjectProxy.deleteCollectionObject(id);
    }
}
