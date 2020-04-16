package org.collectionspace.services.common.vocabulary;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.AbstractServiceContextImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.vocabulary.RefNameServiceUtils.AuthRefConfigInfo;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.nuxeo.client.java.CoreSessionInterface;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;

/**
 * A DocumentModelList representing all of the documents that potentially reference an
 * authority item, found via full text search. This list must be post-processed to
 * eliminate false positives.
 *
 * Documents in this list are lazily fetched one page at a time, as they are accessed through
 * the list's Iterator, retrieved with the iterator() method. List items may not be accessed
 * through any other means, including the get() method, and the ListIterator retrieved
 * with listIterator(). Attempts to do so will result in unspecified behavior.
 * 
 */
public class LazyAuthorityRefDocList extends DocumentModelListImpl {
    private static final Logger logger = LoggerFactory.getLogger(LazyAuthorityRefDocList.class);
	private static final long serialVersionUID = 1L;
	
	private ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx;
	private RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient;
	private CoreSessionInterface repoSession;
	private List<String> serviceTypes;
	private String refName;
	private String refPropName;
	private Map<String, ServiceBindingType> queriedServiceBindings;
	private Map<String, List<AuthRefConfigInfo>> authRefFieldsByService;
	private String whereClauseAdditions;
	private String orderByClause;
	private int pageSize;
	
	private DocumentModelList firstPageDocList;
	
	/**
	 * Creates a LazyAuthorityRefDocList. The method signature is modeled after
	 * RefNameServiceUtils.findAuthorityRefDocs (the non-lazy way of doing this).
	 * 
	 * @param ctx
	 * @param repoClient
	 * @param repoSession
	 * @param serviceTypes
	 * @param refName
	 * @param refPropName
	 * @param queriedServiceBindings
	 * @param authRefFieldsByService
	 * @param whereClauseAdditions
	 * @param orderByClause
	 * @param pageSize						The number of documents to retrieve in each page
	 * @param computeTotal
	 * @throws DocumentException
	 * @throws DocumentNotFoundException
	 */
	public LazyAuthorityRefDocList(
	        ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
	        RepositoryClient<PoxPayloadIn, PoxPayloadOut> repoClient,
	        CoreSessionInterface repoSession, List<String> serviceTypes,
	        String refName,
	        String refPropName,
	        Map<String, ServiceBindingType> queriedServiceBindings,
	        Map<String, List<AuthRefConfigInfo>> authRefFieldsByService,
	        String whereClauseAdditions,
	        String orderByClause,
	        int pageSize,
	        boolean useDefaultOrderByClause,
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

		// Fetch the first page immediately. This is necessary so that calls
		// to totalSize() will work immediately. The computeTotal flag is passed
		// into this initial page fetch. There's no need to compute totals
		// when fetching subsequent pages.
		
		firstPageDocList = fetchPage(0, computeTotal, useDefaultOrderByClause);
	}

	/**
	 * Retrieves a page of authority references.
	 * 
	 * @param pageNum		The page number
	 * @param computeTotal	
	 * @return
	 * @throws DocumentNotFoundException
	 * @throws DocumentException
	 */
	private DocumentModelList fetchPage(int pageNum, boolean computeTotal, boolean useDefaultOrderByClause) throws DocumentNotFoundException, DocumentException {
		return RefNameServiceUtils.findAuthorityRefDocs(ctx, 
		        repoClient, 
		        repoSession,
		        serviceTypes, 
		        refName, 
		        refPropName, 
		        queriedServiceBindings, 
		        authRefFieldsByService,
		        whereClauseAdditions, 
		        orderByClause, 
		        pageNum, 
		        pageSize,
		        useDefaultOrderByClause,
		        computeTotal);
	}
		
	@Override
	public long totalSize() {
		// Return the totalSize from the first page of documents.
		return firstPageDocList.totalSize();
	}

	@Override
	public Iterator<DocumentModel> iterator() {
		// Create a new iterator that starts with the first page of documents.
		return new Itr(0, firstPageDocList);
	}
	
	/**
	 * An iterator over a LazyAuthorityRefDocList. The iterator keeps one
	 * page of documents in memory at a time, and traverses that page until
	 * no items remain. A new page is fetched only when the current page is
	 * exhausted.
	 *
	 */
	private class Itr extends AbstractIterator<DocumentModel> {
		private int currentPageNum = 0;
		private DocumentModelList currentPageDocList;
		private Iterator<DocumentModel> currentPageIterator;
		
		/**
		 * Creates a new iterator.
		 * 
		 * @param currentPageNum		The initial page number
		 * @param currentPageDocList	The documents in the initial page
		 */
		protected Itr(int pageNum, DocumentModelList pageDocList) {
			setCurrentPage(pageNum, pageDocList);
		}

		/**
		 * Changes the current page.
		 * 
		 * @param pageNum		The new page number
		 * @param pageDocList	The documents in the new page
		 */
		private void setCurrentPage(int pageNum, DocumentModelList pageDocList) {
			this.currentPageNum = pageNum;
			this.currentPageDocList = pageDocList;
			this.currentPageIterator = pageDocList.iterator();
		}
		
		@Override
		protected DocumentModel computeNext() {
			// Find the next document to return, looking first in the current
			// page. If the current page is exhausted, fetch the next page.
			
			if (currentPageIterator.hasNext()) {
				// There is still an element to return from the current page.
				return currentPageIterator.next();
			}
			
			// The current page is exhausted.
			
			if (pageSize == 0 || (currentPageDocList.size() < pageSize)) { 
				// There are no more pages.
				return endOfData();
			}
			
			// There may be more pages. Try to fetch the next one.
			
			int nextPageNum = currentPageNum + 1;
			DocumentModelList nextPageDocList = null;
			
			try {
				nextPageDocList = fetchPage(nextPageNum, false, true);
			}
			catch(DocumentException e) {
				logger.error(e.getMessage());
			}
			
			if (nextPageDocList == null || nextPageDocList.size() == 0) {
				// There are no more pages.
				return endOfData();
			}

			// There is another page. Make it the current page.
			
			setCurrentPage(nextPageNum, nextPageDocList);
			
			if (currentPageIterator.hasNext()) {
				return currentPageIterator.next();
			}

			// Shouldn't get here.

			return endOfData();
		}
	}
}
