package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.vocabulary.VocabulariesCommonList;
import org.collectionspace.services.vocabulary.VocabularyitemsCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

/**
 * A VocabularyClient.

 * @version $Revision:$
 */
public class VocabularyClient extends BaseServiceClient {

	/* (non-Javadoc)
	 * @see org.collectionspace.services.client.BaseServiceClient#getServicePathComponent()
	 */
	public String getServicePathComponent() {
		return "vocabularies";
	}
	
	public String getItemCommonPartName() {
		return getCommonPartName("vocabularyitems");
	}

    /**
     *
     */
    private static final VocabularyClient instance = new VocabularyClient();
    /**
     *
     */
    private VocabularyProxy vocabularyProxy;

    /**
     *
     * Default constructor for VocabularyClient class.
     *
     */
    public VocabularyClient() {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        RegisterBuiltin.register(factory);
        vocabularyProxy = ProxyFactory.create(VocabularyProxy.class, getBaseURL());
    }

    /**
     * FIXME Comment this
     *
     * @return
     */
    public static VocabularyClient getInstance() {
        return instance;
    }

    /**
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#readList()
     */
    public ClientResponse<VocabulariesCommonList> readList() {
        return vocabularyProxy.readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#read(java.lang.String)
     */

    public ClientResponse<MultipartInput> read(String csid) {
        return vocabularyProxy.read(csid);
    }

    /**
     * @param vocabulary
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#createVocabulary(org.collectionspace.hello.Vocabulary)
     */
    public ClientResponse<Response> create(MultipartOutput multipart) {
        return vocabularyProxy.create(multipart);
    }

    /**
     * @param csid
     * @param vocabulary
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#updateVocabulary(java.lang.Long, org.collectionspace.hello.Vocabulary)
     */
    public ClientResponse<MultipartInput> update(String csid, MultipartOutput multipart) {
        return vocabularyProxy.update(csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#deleteVocabulary(java.lang.Long)
     */
    public ClientResponse<Response> delete(String csid) {
        return vocabularyProxy.delete(csid);
    }

    /**
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#readItemList()
     */
    public ClientResponse<VocabularyitemsCommonList> readItemList(String vcsid) {
        return vocabularyProxy.readItemList(vcsid);
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#read(java.lang.String)
     */

    public ClientResponse<MultipartInput> readItem(String vcsid, String csid) {
        return vocabularyProxy.readItem(vcsid, csid);
    }

    /**
     * @param vocabulary
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#createVocabulary(org.collectionspace.hello.Vocabulary)
     */
    public ClientResponse<Response> createItem(String vcsid, MultipartOutput multipart) {
        return vocabularyProxy.createItem(vcsid, multipart);
    }

    /**
     * @param csid
     * @param vocabulary
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#updateVocabulary(java.lang.Long, org.collectionspace.hello.Vocabulary)
     */
    public ClientResponse<MultipartInput> updateItem(String vcsid, String csid, MultipartOutput multipart) {
        return vocabularyProxy.updateItem(vcsid, csid, multipart);

    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.services.client.VocabularyProxy#deleteVocabulary(java.lang.Long)
     */
    public ClientResponse<Response> deleteItem(String vcsid, String csid) {
        return vocabularyProxy.deleteItem(vcsid, csid);
    }
}
