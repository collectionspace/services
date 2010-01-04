package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.organization.OrgauthoritiesCommonList;
import org.collectionspace.services.organization.OrganizationsCommonList;
import org.collectionspace.services.client.OrgAuthorityProxy;

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
public class OrgAuthorityClient extends BaseServiceClient {

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
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
        orgAuthorityProxy = ProxyFactory.create(OrgAuthorityProxy.class, getBaseURL());
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
}
