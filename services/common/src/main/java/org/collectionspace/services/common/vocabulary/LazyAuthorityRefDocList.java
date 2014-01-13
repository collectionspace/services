package org.collectionspace.services.common.vocabulary;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.core.api.repository.RepositoryInstance;

import com.google.common.collect.AbstractIterator;

public class LazyAuthorityRefDocList extends DocumentModelListImpl {
	public static final int DEFAULT_PAGE_SIZE = 100;
	
	private static final long serialVersionUID = 1L;
	
	private ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx;
	private RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient;
	private RepositoryInstance repoSession;
	private List<String> serviceTypes;
	private String refName;
	private String refPropName;
	private Map<String, ServiceBindingType> queriedServiceBindings;
	private Map<String, List<AuthRefConfigInfo>> authRefFieldsByService;
	private String whereClauseAdditions;
	private String orderByClause;
	private int pageSize = DEFAULT_PAGE_SIZE;
	
	private DocumentModelList firstPageDocList;
	
	public LazyAuthorityRefDocList(
            ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
            RepositoryInstance repoSession, List<String> serviceTypes,
            String refName,
            String refPropName,
            Map<String, ServiceBindingType> queriedServiceBindings,
            Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
            String whereClauseAdditions,
            String orderByClause,
            int pageSize,
            boolean computeTotal) throws DocumentException, DocumentNotFoundException {

		this.ctx = ctx;
		this.repoClient = repoClient;
		this.repoSession = repoSession;
		this.serviceTypes = serviceTypes;
		this.refName = refName;
		this.refPropName = refPropName;
		this.queriedServiceBindings = queriedServiceBindings;
		this.authRefFieldsByService = authRefFieldsByService;
		this.whereClauseAdditions = whereClauseAdditions;
		this.orderByClause = orderByClause;
		this.pageSize = pageSize;
		
		// If totals should be computed, do it on the initial page fetch.
		// There's probably no need to do it when fetching subsequent pages.
		
		firstPageDocList = fetchPage(0, computeTotal);
	}

	private DocumentModelList fetchPage(int pageNum, boolean computeTotal) throws DocumentNotFoundException, DocumentException {
		return RefNameServiceUtils.findAuthorityRefDocs(ctx, repoClient, repoSession,
                serviceTypes, refName, refPropName, queriedServiceBindings, authRefFieldsByService,
                whereClauseAdditions, orderByClause, pageSize, pageNum, computeTotal);
	}
		
	@Override
	public long totalSize() {
		return firstPageDocList.totalSize();
	}

	@Override
	public Iterator<DocumentModel> iterator() {
		return new Itr(0, firstPageDocList);
	}
	
	private class Itr extends AbstractIterator<DocumentModel> {
		private DocumentModelList currentPageDocList;
		private int currentPageNum = 0;
		private Iterator<DocumentModel> currentPageIterator;
		
		protected Itr(int currentPageNum, DocumentModelList currentPageDocList) {
			this.currentPageNum = currentPageNum;
			this.currentPageDocList = currentPageDocList;
			this.currentPageIterator = currentPageDocList.iterator();
		}
		
		@Override
		protected DocumentModel computeNext() {
			if (currentPageIterator.hasNext()) { 
				// There are still elements to return in the current page.
				return currentPageIterator.next();
			}
			
			// We've exhausted the current page.
			
			if (currentPageDocList.size() < pageSize) { 
				// There are no more pages.
				return endOfData();
			}
			
			// There may be more pages. Try to fetch the next one.
			
			int nextPageNum = currentPageNum + 1;
			DocumentModelList nextPageDocList = null;
			
			try {
				nextPageDocList = fetchPage(nextPageNum, false);
			}
			catch(DocumentException e) {}
			
			if (nextPageDocList == null || nextPageDocList.size() == 0) {
				// There are no more pages.
				return endOfData();					
			}

			// We successfully retrieved another page. Make it the current page.
			
			currentPageNum = nextPageNum;
			currentPageDocList = nextPageDocList;
			currentPageIterator = nextPageDocList.iterator();
			
			if (!currentPageIterator.hasNext()) {
				// Shouldn't get here, since we already checked that the size isn't 0.
				return endOfData();
			}

			return currentPageIterator.next();
		}
	}
}
