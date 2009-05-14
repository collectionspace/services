package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.People;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A PersonNuxeoClient.

 * @version $Revision:$
 */
public class MultischemaClient extends CollectionSpaceClient {

    /**
     *
     */
    private static final MultischemaClient instance = new MultischemaClient();
    /**
     *
     */
    private MultischemaProxy proxy;

    /**
     *
     * Create a new PersonNuxeoClient.
     *
     */
    private MultischemaClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        proxy = ProxyFactory.create(MultischemaProxy.class, getURL());
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static MultischemaClient getInstance() {
        return instance;
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#getPerson(java.lang.String)
     */
    public ClientResponse<MultipartFormDataInput> getPerson(String id) {
        return proxy.getPerson(id);
    }

    /**
     * @param person
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#createPerson(org.collectionspace.hello.PersonNuxeo)
     */
    public ClientResponse<Response> createPerson(MultipartFormDataOutput multipartPerson) {
        return proxy.createPerson(multipartPerson);
    }

    /**
     * @param id
     * @param person
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#updatePerson(java.lang.Long, org.collectionspace.hello.PersonNuxeo)
     */
    public ClientResponse<PersonNuxeo> updatePerson(String id, PersonNuxeo person) {
        return proxy.updatePerson(id, person);
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#deletePerson(java.lang.Long)
     */
    public ClientResponse<Response> deletePerson(String id) {
        return proxy.deletePerson(id);
    }
}
