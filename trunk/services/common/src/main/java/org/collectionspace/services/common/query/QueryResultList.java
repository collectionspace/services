package org.collectionspace.services.common.query;

import java.util.List;
import org.collectionspace.services.common.document.DocumentListWrapper;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryResultList.
 */
public class QueryResultList<LISTTYPE> {
	
	private long totalSize = 0;
	
	/** The wrapper object list. */
	private LISTTYPE wrapperObjectList;

	/**
	 * Instantiates a new query result list.
	 */
	private QueryResultList() {
		//private constructor
	}
	
	/**
	 * Instantiates a new query result list.
	 *
	 * @param theWrapperObjectList the the wrapper object list
	 */
	public QueryResultList(LISTTYPE theWrapperObjectList) {
		wrapperObjectList = theWrapperObjectList;
	}
	
	/**
	 * Gets the wrapper object list.
	 *
	 * @return the wrapper object list
	 */
	public LISTTYPE getWrapperObjectList() {
		return this.wrapperObjectList;
	}
	
	/**
	 * Sets the total size.  This is the total size of the non-paged result set.
	 *
	 * @param theTotalResultSize the new total size
	 */
	public void setTotalSize(long theTotalSize) {
		totalSize = theTotalSize;
	}

	public long getTotalSize() {
		return totalSize;
	}
}
