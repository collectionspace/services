package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.DomainIdentifier;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A IdentifierClient.

 * @version $Revision:$
 */
public class DomainIdentifierClient implements CollectionSpaceClient {


    /**
     *
     */
    private static final DomainIdentifierClient instance = new DomainIdentifierClient();
    /**
     *
     */
    private DomainIdentifierProxy identifierProxy;

    /**
     *
     * Create a new IdentifierClient.
     *
     */
    private DomainIdentifierClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        identifierProxy = ProxyFactory.create(DomainIdentifierProxy.class, HOST + URI);
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static DomainIdentifierClient getInstance() {
        return instance;
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.IdentifierProxy#getIdentifier(java.lang.Long)
     */
    public ClientResponse<DomainIdentifier> getIdentifier(String id) {
        return identifierProxy.getIdentifier(id);
    }

    /**
     * @param identifier
     * @return
     * @see org.collectionspace.hello.client.IdentifierProxy#createIdentifier(org.collectionspace.hello.client.entity.Identifier)
     */
    public ClientResponse<Response> createIdentifier(DomainIdentifier identifier) {
        return identifierProxy.createIdentifier(identifier);
    }
}
