package org.collectionspace.hello.client;

import javax.ws.rs.core.Response;

import org.collectionspace.hello.Identifier;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A IdentifierClient.

 * @version $Revision:$
 */
public class IdentifierClient {

    /**
     *
     */
    private static final IdentifierClient instance = new IdentifierClient();
    /**
     *
     */
    private IdentifierProxy identifierProxy;

    /**
     *
     * Create a new IdentifierClient.
     *
     */
    private IdentifierClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        identifierProxy = ProxyFactory.create(IdentifierProxy.class, "http://localhost:8080/helloworld/cspace");
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static IdentifierClient getInstance() {
        return instance;
    }

    /**
     * @param id
     * @return
     * @see org.collectionspace.hello.client.IdentifierProxy#getIdentifier(java.lang.Long)
     */
    public ClientResponse<Identifier> getIdentifier(Long id) {
        return identifierProxy.getIdentifier(id);
    }

    /**
     * @param identifier
     * @return
     * @see org.collectionspace.hello.client.IdentifierProxy#createIdentifier(org.collectionspace.hello.client.entity.Identifier)
     */
    public ClientResponse<Response> createIdentifier(Identifier identifier) {
        return identifierProxy.createIdentifier(identifier);
    }
}
