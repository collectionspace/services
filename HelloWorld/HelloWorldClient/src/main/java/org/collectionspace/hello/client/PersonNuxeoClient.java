package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.PersonNuxeo;
import org.collectionspace.hello.People;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A PersonNuxeoClient.

 * @version $Revision:$
 */
public class PersonNuxeoClient implements CollectionSpaceClient {

    /**
     *
     */
    private static final PersonNuxeoClient instance = new PersonNuxeoClient();
    /**
     *
     */
    private PersonNuxeoProxy personProxy;

    /**
     *
     * Create a new PersonNuxeoClient.
     *
     */
    private PersonNuxeoClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        personProxy = ProxyFactory.create(PersonNuxeoProxy.class, HOST + URI);
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static PersonNuxeoClient getInstance() {
        return instance;
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#getPerson()
     */
    public ClientResponse<People> getPeople() {
        return personProxy.getPeople();
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#getPerson(java.lang.String)
     */
    public ClientResponse<PersonNuxeo> getPerson(String id) {
        return personProxy.getPerson(id);
    }

    /**
     * @param person
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#createPerson(org.collectionspace.hello.PersonNuxeo)
     */
    public ClientResponse<Response> createPerson(PersonNuxeo person) {
        return personProxy.createPerson(person);
    }

    /**
     * @param id
     * @param person
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#updatePerson(java.lang.Long, org.collectionspace.hello.PersonNuxeo)
     */
    public ClientResponse<PersonNuxeo> updatePerson(String id, PersonNuxeo person) {
        return personProxy.updatePerson(id, person);
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.PersonNuxeoProxy#deletePerson(java.lang.Long)
     */
    public ClientResponse<Response> deletePerson(String id) {
        return personProxy.deletePerson(id);
    }
}
