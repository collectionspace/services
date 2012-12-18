package org.collectionspace.services.common.storage.jpa;

import java.util.List;

import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.document.AbstractDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.nuxeo.ecm.core.api.DocumentModel;

public abstract class JpaDocumentHandler<T, TL, WT, WLT>
	extends AbstractDocumentHandlerImpl<T, TL, WT, WLT>{

	@Override
	protected String getRefnameDisplayName(DocumentWrapper<WT> wrapDoc) {
		return ""; // Empty string since we don't yet support this feature in JPA documents
	}
	
	@Override
	public RefName.RefNameInterface getRefName(DocumentWrapper<WT> docWrapper, String tenantName, String serviceName) {
		//
		// Not implemented
		//
		return null;
	}
	
    /**
     * Extract paging info.
     *
     * @param commonsList the commons list
     * @return the tL
     * @throws Exception the exception
     */
    public TL extractPagingInfo(TL theCommonList, DocumentWrapper<WLT> wrapDoc)
            throws Exception {
        AbstractCommonList commonList = (AbstractCommonList) theCommonList;

        DocumentFilter docFilter = this.getDocumentFilter();
        long pageSize = docFilter.getPageSize();
        long pageNum = pageSize != 0 ? docFilter.getOffset() / pageSize : pageSize;
        // set the page size and page number
        commonList.setPageNum(pageNum);
        commonList.setPageSize(pageSize);
        List docList = (List)wrapDoc.getWrappedObject();
        // Set num of items in list. this is useful to our testing framework.
        commonList.setItemsInPage(docList.size());
        // set the total result size
        commonList.setTotalItems(docList.size());

        return (TL) commonList;
    }
    
    public Lifecycle getLifecycle(String docTypeName) {
    	Lifecycle result = new Lifecycle();
    	result.setName("Life cycles are not supported by the JPA-based services.");
    	return result; // NOTE: As of 3/2012, none of the JPA-based services support a life cycle type.
    }
    
    @Override
    public Lifecycle getLifecycle() {
    	return getLifecycle(null); // NOTE: As of 3/2012, none of the JPA-based services support a life cycle type.
    }
    
	@Override
	public void handleWorkflowTransition(
			DocumentWrapper<DocumentModel> wrapDoc, TransitionDef transitionDef)
			throws Exception {
		// Do nothing.  JPA document handlers do not support workflow transitions yet.
	}
    
}
