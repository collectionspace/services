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
import org.collectionspace.services.client.IClientQueryParams;
import org.collectionspace.services.common.query.IQueryManager;
import org.collectionspace.services.common.context.ServiceContext;

//TODO: would be great to not rely on resteasy directly
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

/**
 * DocumentFilter bundles simple query filtering parameters. 
 * It is designed to be used with filtered get and search calls to RepositoryClient.
 * The values are set up and stored on a DocumentHandler, and
 * fetched by a RepositoryClient when calling filtered get methods.
 */
public class DocumentFilter {

    public static final int DEFAULT_PAGE_SIZE_INIT = 40;
    public static final String PAGE_SIZE_DEFAULT_PROPERTY = "pageSizeDefault";
    public static int defaultPageSize = DEFAULT_PAGE_SIZE_INIT;

    protected String whereClause;	// Filtering clause. Omit the "WHERE".
    protected int startPage;		// Pagination offset for list results
    protected int pageSize;			// Pagination limit for list results
    private boolean pageSizeDirty = false; // True if default page size explicitly set/overridden

    //queryParams is not initialized as it would require a multi-valued map implementation
    //unless it is used from opensource lib...this variable holds ref to
    //implementation available in JBoss RESTeasy
    private MultivaluedMap<String, String> queryParams = null;

    /**
     * ParamBinding encapsulates parameter binding for query
     */
    public static class ParamBinding {

        private String name;
        private Object value;

        public ParamBinding(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return the value
         */
        public Object getValue() {
            return value;
        }

        /**
         * @param value the value to set
         */
        public void setValue(Object value) {
            this.value = value;
        }
    }

    /**
     * Instantiates a new document filter.
     * 
     * @param ctx the ctx
     */
    public DocumentFilter(ServiceContext ctx) {
        this.setPageSize(ctx.getServiceBindingPropertyValue(
                DocumentFilter.PAGE_SIZE_DEFAULT_PROPERTY));
    }

    public DocumentFilter() {
        this("", 0, defaultPageSize);			// Use empty string for easy concatenation
    }

    public DocumentFilter(String whereClause, int startPage, int pageSize) {
        this.whereClause = whereClause;
        this.startPage = (startPage > 0) ? startPage : 0;
        this.pageSize = (pageSize > 0) ? pageSize : defaultPageSize;
    }

    /**
     * Sets the pagination.
     * 
     * @param queryParams the query params
     */
    public void setPagination(MultivaluedMap<String, String> queryParams) {
        //
        // Bail if there are no params
        //
        if (queryParams == null) {
            return;
        }

        //
        // Set the page size
        //
        String pageSizeStr = null;
        List<String> list = queryParams.remove(IClientQueryParams.PAGE_SIZE_PARAM);  //REM: Should we really be removing this param?
        if (list != null) {
            pageSizeStr = list.get(0);
        }
        setPageSize(pageSizeStr);

        //
        // Set the start page
        //
        String startPageStr = null;
        list = queryParams.remove(IClientQueryParams.START_PAGE_PARAM);	//REM: Should we really be removing this param?
        if (list != null) {
            startPageStr = list.get(0);
        }
        setStartPage(startPageStr);
    }

    /**
     * @return the current default page size for new DocumentFilter instances
     */
    public static int getDefaultPageSize() {
        return defaultPageSize;
    }

    /**
     * @param defaultPageSize the working default page size for new DocumentFilter instances
     */
    public static void setDefaultPageSize(int defaultPageSize) {
        DocumentFilter.defaultPageSize = defaultPageSize;
    }

    /**
     * @return the WHERE filtering clause
     */
    public String getWhereClause() {
        return whereClause;
    }

    /**
     * @param whereClause the filtering clause (do not include "WHERE")
     */
    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public void appendWhereClause(String whereClause) {
        String currentClause = getWhereClause();
        if (currentClause != null) {
            String newClause = currentClause.concat(IQueryManager.SEARCH_TERM_SEPARATOR + whereClause);
            this.setWhereClause(newClause);
        }
    }

    /**
     * buildWhereClause builds where clause for search query
     * @param queryStrBldr query string to append with where clause
     * @return parameter binding
     */
    public List<ParamBinding> buildWhereForSearch(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }

    /**
     * buildWhereClause builds where clause for get, update or delete
     * @param queryStrBldr query string to append with where clause
     * @return parameter binding
     */
    public List<ParamBinding> buildWhere(StringBuilder queryStrBldr) {
        return new ArrayList<ParamBinding>();
    }

    /**
     * @return the specified (0-based) page offset
     */
    public int getStartPage() {
        return startPage;
    }

    /**
     * @param startPage the (0-based) page offset to use
     */
    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    /**
     * @return the max number of items to return for list requests
     */
    public int getPageSize() {
        return pageSize;
    }

    public boolean getPageSizeDirty() {
        return this.getPageSizeDirty();
    }

    /**
     * @param pageSize the max number of items to return for list requests
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.pageSizeDirty = true; // page size explicity set/overriden
    }

    /**
     * @param pageSize the max number of items to return for list requests
     */
    public void setPageSize(String pageSizeStr) {
        int pageSize = this.defaultPageSize;
        if (pageSizeStr != null) {
            try {
                pageSize = Integer.valueOf(pageSizeStr);
            } catch (NumberFormatException e) {
                //FIXME This should cause a warning in the log file and should result in the
                //FIXME page size being set to the default.  We don't need to throw an exception here.
                throw new NumberFormatException("Bad value for: " +
                		IClientQueryParams.PAGE_SIZE_PARAM);
            }
        }

        setPageSize(pageSize);
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
                throw new NumberFormatException("Bad value for: " +
                		IClientQueryParams.START_PAGE_PARAM);
            }
        }
    }

    /**
     * @return the offset computed from the startPage and the pageSize
     */
    public int getOffset() {
        return pageSize * startPage;
    }

    public void addQueryParam(String key, String value) {
        if (queryParams != null) {
            queryParams.add(key, value);
        }
    }

    public List<String> getQueryParam(String key) {
        if (queryParams != null) {
            return queryParams.get(key);
        } else {
            return new ArrayList<String>();
        }
    }

    public MultivaluedMap<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(MultivaluedMap<String, String> queryParams) {
        this.queryParams = queryParams;
    }
}
