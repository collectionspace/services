/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.common.document;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import org.collectionspace.authentication.AuthN;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.common.context.ServiceContext;

/**
 * The Class DocumentFilter.
 */
//FIXME: it would be nice to instantiate the doc filter with the service context
//so tenant context and other things available from the service context
//could be utilized while building the where clause.
public abstract class DocumentFilter {

    /** The Constant DEFAULT_PAGE_SIZE_INIT. */
    public static final int DEFAULT_PAGE_SIZE_INIT = 40; 		// Default page size if one is specified in the service-config.xml
    public static final int DEFAULT_PAGE_SIZE_MAX_INIT = 1000;	// Default page size max if one is specified in the service-config.xml
    public static final String DEFAULT_SELECT_CLAUSE = "SELECT * FROM ";
    
    /** The Constant PAGE_SIZE_DEFAULT_PROPERTY. */
    public static final String PAGE_SIZE_DEFAULT_PROPERTY = "pageSizeDefault";
    public static final String PAGE_SIZE_MAX_PROPERTY = "pageSizeMax";
    
    /** The select clause. */
    protected String selectClause;
    /** The where clause. */
    protected String whereClause;	// Filtering clause. Omit the "WHERE".
    /** The order by clause. */
    protected String orderByClause;	// Filtering clause. Omit the "ORDER BY".
    public static final String EMPTY_ORDER_BY_CLAUSE = "";
    public static final String ORDER_BY_LAST_UPDATED = CollectionSpaceClient.CORE_UPDATED_AT + " DESC";
    public static final String ORDER_BY_CREATED_AT = CollectionSpaceClient.CORE_CREATED_AT + " DESC";

    /** The max page size. */
    protected int pageSizeMax = DEFAULT_PAGE_SIZE_MAX_INIT;		// The largest page size allowed.  Can be overridden in the service-config.xml
    
    /** The default page size. */
    public static int defaultPageSize = DEFAULT_PAGE_SIZE_INIT; // Default page size if one is specified in the service-config.xml
    
    /** The start page. */
    protected int startPage;		// Pagination offset for list results
    
    /** The page size. */
    protected int pageSize;			// Pagination limit for list results
    
    /** Flag to see if we should use default orderBy clause */
    protected boolean useDefaultOrderByClause = true;
    
    /** The total number of items for a query result -independent of paging restrictions.
     * 
     */
    protected long totalItemsResult = -1;
    
    //queryParams is not initialized as it would require a multi-valued map implementation
    //unless it is used from opensource lib...this variable holds ref to
    //implementation available in JBoss RESTeasy
    /** The query params. */
    private MultivaluedMap<String, String> queryParams = null;

    /**
     * The Class ParamBinding.
     */
    public static class ParamBinding {

        /** The name. */
        private String name;
        /** The value. */
        private Object value;

        /**
         * Instantiates a new param binding.
         *
         * @param theName the name
         * @param theValue the value
         */
        public ParamBinding(String theName, Object theValue) {
            this.name = theName;
            this.value = theValue;
        }

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name.
         *
         * @param theName the new name
         */
        public void setName(String theName) {
            this.name = theName;
        }

        /**
         * Gets the value.
         *
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * Sets the value.
         *
         * @param theValue the new value
         */
        public void setValue(Object theValue) {
            this.value = theValue;
        }
    }

    /**
     * Instantiates a new document filter.
     *
     * @param ctx the ctx
     */
    public DocumentFilter(ServiceContext ctx) {
    	// Ignore errors - some contexts do not have proper service binding info
    	try {
    		String pageSizeMaxString = ctx.getServiceBindingPropertyValue(
                    DocumentFilter.PAGE_SIZE_MAX_PROPERTY); 
    		this.setPageSizeMax(pageSizeMaxString);

    		String pageSizeString = ctx.getServiceBindingPropertyValue(
                    DocumentFilter.PAGE_SIZE_DEFAULT_PROPERTY); 
    		this.setPageSize(pageSizeString);    		
    	} catch(Exception e) {
    		this.setPageSize(defaultPageSize);
    	} 
    }

    /**
     * Instantiates a new document filter.
     */
    public DocumentFilter() {
        this("", 0, defaultPageSize);			// Use empty string for easy concatenation
    }

    /**
     * Instantiates a new document filter.
     *
     * @param theWhereClause the where clause
     * @param theStartPage the start page
     * @param thePageSize the page size
     */
    public DocumentFilter(String theWhereClause, int theStartPage, int thePageSize) {
        this(DEFAULT_SELECT_CLAUSE, theWhereClause, EMPTY_ORDER_BY_CLAUSE, theStartPage, thePageSize);
    }

    /**
     * Instantiates a new document filter.
     *
     * @param theWhereClause the where clause
     * @param theOrderByClause the order by clause
     * @param theStartPage the start page
     * @param thePageSize the page size
     */
    public DocumentFilter(String theSelectClause, String theWhereClause, String theOrderByClause, int theStartPage, int thePageSize) {
    	this.selectClause = theSelectClause;
        this.whereClause = theWhereClause;
        this.orderByClause = theOrderByClause;
        this.startPage = (theStartPage > 0) ? theStartPage : 0;
        this.pageSize = (thePageSize > 0) ? thePageSize : defaultPageSize;
    }
    
    public void setUseDefaultOrderByClause(boolean flag) {
        this.useDefaultOrderByClause = flag;
    }
    
    public boolean getUseDefaultOrderByClause() {
        return this.useDefaultOrderByClause;
    }

    /**
     * Sets the pagination.
     *
     * @param theQueryParams the the query params
     */
    public void setPagination(MultivaluedMap<String, String> theQueryParams) {
        //
        // Bail if there are no params
        //
        if (theQueryParams == null) {
            return;
        }
        //
        // Set the page size
        //
        String pageSizeStr = null;
        List<String> list = theQueryParams.get(IClientQueryParams.PAGE_SIZE_PARAM);
        if (list != null) {
            pageSizeStr = list.get(0);
        }
        setPageSize(pageSizeStr);
        //
        // Set the start page
        //
        String startPageStr = null;
        list = theQueryParams.get(IClientQueryParams.START_PAGE_PARAM);
        if (list != null) {
            startPageStr = list.get(0);
        }
        setStartPage(startPageStr);
    }

    /**
     * Gets the default page size.
     *
     * @return the default page size
     */
    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    /**
     * Sets the default page size.
     *
     * @param theDefaultPageSize the new default page size
     */
    public static void setDefaultPageSize(int theDefaultPageSize) {
        DocumentFilter.defaultPageSize = theDefaultPageSize;
    }

    /**
     * Gets the select clause.
     *
     * @return the select clause
     */
    public String getSelectClause() {
        return selectClause != null ? selectClause : DEFAULT_SELECT_CLAUSE;
    }
    
    public String getJoinFetchClause() {
    	return null;
    }
    
    /**
     * Gets the where clause.
     *
     * @return the where clause
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * Sets the select clause.
     *
     * @param theSelectClause the new select clause
     */
    public void setSelectClause(String theSelectClause) {
        this.selectClause = theSelectClause;
    }

    /**
     * Sets the where clause.
     *
     * @param theWhereClause the new where clause
     */
    public void setWhereClause(String theWhereClause) {
        this.whereClause = theWhereClause;
    }

    /**
     * Append where clause.
     *
     * @param theWhereClause the where clause
     * @param conjunction the conjunction to insert between the current
     *        where clause, if any, and the additional where clause to be appended
     */
    public void appendWhereClause(String theWhereClause, String conjunction) {
        if (theWhereClause != null && ! theWhereClause.trim().isEmpty()) {
            String currentClause = getWhereClause();
            if (currentClause != null) {
                String newClause = currentClause.concat(conjunction + theWhereClause);
                this.setWhereClause(newClause);
            } else {
                this.setWhereClause(theWhereClause);
            }
        }
    }

    /**
     * Builds the where for search.
     *
     * @param queryStrBldr the query str bldr
     * @return the list
     */
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }

    /**
     * Builds the where.
     *
     * @param queryStrBldr the query str bldr
     * @return the list
     */
    public List<ParamBinding> buildWhere(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }

    /**
     * Sets the sort ordering.
     *
     * @param theQueryParams the query params
     */
    public void setSortOrder(MultivaluedMap<String, String> theQueryParams) {
        // Bail if there are no params
        if (theQueryParams == null) {
            return;
        }
        // Set the order by clause
        String orderByStr = null;
        List<String> list = theQueryParams.get(IClientQueryParams.ORDER_BY_PARAM);
        if (list != null) {
            orderByStr = list.get(0);
        }

        // FIXME: Verify the format of the value(s) in the 'sort by'
        // query param.

        setOrderByClause(orderByStr);
    }

    /**
     * Gets the order by clause.
     *
     * @return the order by clause
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * Sets the order by clause.
     *
     * @param theOrderByClause the new order by clause
     */
    public void setOrderByClause(String theOrderByClause) {
        this.orderByClause = theOrderByClause;
    }

    /**
     * Gets the start page.
     *
     * @return the start page
     */
    public int getStartPage() {
        return startPage;
    }

    /**
     * Sets the start page.
     *
     * @param theStartPage the new start page
     */
    public void setStartPage(int theStartPage) {
        this.startPage = theStartPage;
    }

    /**
     * Gets the page size.
     *
     * @return the page size
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * Gets the page size dirty.
     *
     * @return the page size dirty
     */
    abstract public boolean getPageSizeDirty();

    /**
     * Sets the page size.
     *
     * @param thePageSize the new page size
     * Never allow the page size to be larger than the 'pageSizeMax' value
     */
    public void setPageSize(int thePageSize) {
    	if (thePageSize > this.pageSizeMax) {
    		this.pageSize = this.pageSizeMax; // page size can't be greater than the max page size
    	} else if (thePageSize == 0) {
    		this.pageSize = this.pageSizeMax; // a page size of 0 means use the max page size
    	} else {
    		this.pageSize = thePageSize;
    	}
    }
    
    /**
     * Sets the page size.
     *
     * @param thePageSizeStr the new page size
     */
    public void setPageSize(String thePageSizeStr) {
        int newPageSize = DocumentFilter.defaultPageSize;
        if (thePageSizeStr != null) {
            try {
                newPageSize = Integer.valueOf(thePageSizeStr);
            } catch (NumberFormatException e) {
                //FIXME This should cause a warning in the log file and should result in the
                //FIXME page size being set to the default.  We don't need to throw an exception here.
                throw new NumberFormatException("Bad value for: "
                        + IClientQueryParams.PAGE_SIZE_PARAM);
            }
        }

        setPageSize(newPageSize);
    }
    
    /**
     * Sets the page size.
     *
     * @param thePageSizeStr the new page size
     */
    public void setPageSizeMax(String thePageSizeMaxStr) {
        int newPageSizeMax = DocumentFilter.DEFAULT_PAGE_SIZE_MAX_INIT;
        if (thePageSizeMaxStr != null) {
            try {
                newPageSizeMax = Integer.valueOf(thePageSizeMaxStr);
            } catch (NumberFormatException e) {
                //FIXME This should cause a warning in the log file and should result in the
                //FIXME page size being set to the default.  We don't need to throw an exception here.
                throw new NumberFormatException("Bad value in service-config.xml for: "
                        + PAGE_SIZE_MAX_PROPERTY);
            }
        }

        this.pageSizeMax = newPageSizeMax;
    }
    

    /**
     * Sets the start page.
     *
     * @param startPageStr the new start page
     */
    protected void setStartPage(String startPageStr) {
        if (startPageStr != null) {
            try {
                startPage = Integer.valueOf(startPageStr);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Bad value for: "
                        + IClientQueryParams.START_PAGE_PARAM);
            }
        }
    }

    /**
     * Gets the offset.
     *
     * @return the offset
     */
    public int getOffset() {
        return pageSize * startPage;
    }

    /**
     * Adds the query param.
     *
     * @param key the key
     * @param value the value
     */
    public void addQueryParam(String key, String value) {
        if (queryParams != null) {
            queryParams.add(key, value);
        }
    }

    /**
     * Gets the query param.
     *
     * @param key the key
     * @return the query param
     */
    public List<String> getQueryParam(String key) {
        if (queryParams != null) {
            return queryParams.get(key);
        } else {
            return new ArrayList<String>();
        }
    }

    /**
     * Gets the query params.
     *
     * @return the query params
     */
    public MultivaluedMap<String, String> getQueryParams() {
        return queryParams;
    }

    /**
     * Sets the query params.
     *
     * @param theQueryParams the the query params
     */
    public void setQueryParams(MultivaluedMap<String, String> theQueryParams) {
        this.queryParams = theQueryParams;
    }

    /**
     * getTenantId
     * //FIXME: it would be nice to take tenantId from service context
     * @return
     */
    protected String getTenantId() {
        return AuthN.get().getCurrentTenantId();
    }

    /*
     * Used to set the total number of items matching a query -independent of the paging restrictions.
     */
	public void setTotalItemsResult(long totalItems) {
		totalItemsResult = totalItems;
	}
	
    /*
     * Used to get the total number of items matching the last query made -independent of the paging restrictions.
     */	
	public long getTotalItemsResult() {
		return totalItemsResult;
	}	
}
