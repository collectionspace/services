package org.collectionspace.services.client;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.collectionspace.services.common.authorityref.AuthorityRefList;
import org.collectionspace.services.contact.ContactsCommonList;
import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A OrgAuthorityClient.

 * @version $Revision:$
 */
public class OrgAuthorityClient extends AbstractServiceClientImpl {

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.AbstractServiceClientImpl#getServicePathComponent()
     */
    @Override
    public String getServicePathComponent() {
        return "orgauthorities";
    }

    public String getItemCommonPartName() {
        return getCommonPartName("organizations");
    }
    /**
     *
     */
    private static final OrgAuthorityClient instance = new OrgAuthorityClient();
    /**
     *
     */
    private OrgAuthorityProxy orgAuthorityProxy;

    /**
     *
     * Default constructor for OrgAuthorityClient class.
     *
     */
    public OrgAuthorityClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        setProxy();
    }

    /**
     * allow to reset proxy as per security needs
     */
    public void setProxy() {
        if (useAuth()) {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL(), getHttpClient());
        } else {
            orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class,
                    getBaseURL());
        }
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static OrgAuthorityClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#readList()
     */
    public ClientResponse<OrgauthoritiesCommonList> readList() {
        return orgAuthorityProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#read(java.lang.String)
     */
    public ClientResponse<MultipartInput> read(String csid) {
        return orgAuthorityProxy.read(csid);
    }

    /**
     * @param name
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#readByName(java.lang.String)
     */
    public ClientResponse<MultipartInput> readByName(String name) {
        return orgAuthorityProxy.readByName(name);
    }

    /**
     * @param orgAuthority
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#createOrgAuthority(org.collectionspace.hello.OrgAuthority)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return orgAuthorityProxy.create(multipart);
    }

    /**
     * @param csid
     * @param orgAuthority
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#updateOrgAuthority(java.lang.Long, org.collectionspace.hello.OrgAuthority)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return orgAuthorityProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#deleteOrgAuthority(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return orgAuthorityProxy.delete(csid);
    }

    /**
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#readItemList()
     */
    public ClientResponse<OrganizationsCommonList> readItemList(String vcsid) {
        return orgAuthorityProxy.readItemList(vcsid);
    }

    /**
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#readItemListForNamedAuthority()
     */
    public ClientResponse<OrganizationsCommonList> readItemListForNamedAuthority(String specifier) {
        return orgAuthorityProxy.readItemListForNamedAuthority(specifier);
    }

    public ClientResponse<AuthorityRefList> getItemAuthorityRefs(String parentcsid, String csid) {
        return orgAuthorityProxy.getItemAuthorityRefs(parentcsid, csid);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#read(java.lang.String)
     */
    public ClientResponse<MultipartInput> readItem(String vcsid, String csid) {
        return orgAuthorityProxy.readItem(vcsid, csid);
    }

    /**
     * @param orgAuthority
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#createOrgAuthority(org.collectionspace.hello.OrgAuthority)
     */
    public ClientResponse<Response> createItem(String vcsid, MultipartOutput multipart) {
        return orgAuthorityProxy.createItem(vcsid, multipart);
    }

    /**
     * @param csid
     * @param orgAuthority
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#updateOrgAuthority(java.lang.Long, org.collectionspace.hello.OrgAuthority)
     */
    public ClientResponse<MultipartInput> updateItem(String vcsid, String csid, MultipartOutput multipart) {
        return orgAuthorityProxy.updateItem(vcsid, csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.OrgAuthorityProxy#deleteOrgAuthority(java.lang.Long)
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return orgAuthorityProxy.deleteItem(vcsid, csid);
    }

    public ClientResponse<Response> createContact(String parentcsid,
            String itemcsid, MultipartOutput multipart) {
        return orgAuthorityProxy.createContact(parentcsid, itemcsid, multipart);
    }

    public ClientResponse<MultipartInput> readContact(String parentcsid,
            String itemcsid, String csid) {
        return orgAuthorityProxy.readContact(parentcsid, itemcsid, csid);
    }

    public ClientResponse<ContactsCommonList> readContactList(String parentcsid,
            String itemcsid) {
        return orgAuthorityProxy.readContactList(parentcsid, itemcsid);
    }

    public ClientResponse<MultipartInput> updateContact(String parentcsid,
            String itemcsid, String csid, MultipartOutput multipart) {
        return orgAuthorityProxy.updateContact(parentcsid, itemcsid, csid, multipart);
    }

    public ClientResponse<Response> deleteContact(String parentcsid,
        String itemcsid, String csid) {
        return orgAuthorityProxy.deleteContact(parentcsid,
            itemcsid, csid);
    }

}
