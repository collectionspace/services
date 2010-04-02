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
import org.collectionspace.services.common.query.IQueryManager;

//TODO: would be great to not rely on resteasy directly
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;

/**
 * DocumentFilter bundles simple query filtering parameters. 
 * It is designed to be used with filtered get and search calls to RepositoryClient.
 * The values are set up and stored on a DocumentHandler, and
 * fetched by a RepositoryClient when calling filtered get methods.
 */
public class DocumentFilter {

    public static final String PAGE_SIZE_DEFAULT_PROPERTY = "pageSizeDefault";
    public static final String PAGE_SIZE_PARAM = "pgSz";
    public static final String START_PAGE_PARAM = "pgNum";
    public static final int DEFAULT_PAGE_SIZE_INIT = 40;
    public static int defaultPageSize = DEFAULT_PAGE_SIZE_INIT;
    protected String whereClause;	// Filtering clause. Omit the "WHERE".
    protected int startPage;		// Pagination offset for list results
    protected int pageSize;			// Pagination limit for list results
    private MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String, String>();


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
    
    public DocumentFilter() {
        this("", 0, defaultPageSize);			// Use empty string for easy concatenation
    }

    public DocumentFilter(String whereClause, int startPage, int pageSize) {
        this.whereClause = whereClause;
        this.startPage = (startPage > 0) ? startPage : 0;
        this.pageSize = (pageSize > 0) ? pageSize : defaultPageSize;
    }

    public void setPagination(MultivaluedMap<String, String> queryParams) {

        String startPageStr = null;
        String pageSizeStr = null;
        List<String> list = queryParams.remove(PAGE_SIZE_PARAM);
        if (list != null) {
            pageSizeStr = list.get(0);
        }
        setPageSize(pageSizeStr);
        list = queryParams.remove(START_PAGE_PARAM);
        if (list != null) {
            startPageStr = list.get(0);
        }
        if (startPageStr != null) {
            try {
                startPage = Integer.valueOf(startPageStr);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Bad value for: " + START_PAGE_PARAM);
            }
        }
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

    /**
     * @param pageSize the max number of items to return for list requests
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * @param pageSize the max number of items to return for list requests
     */
    public void setPageSize(String pageSizeStr) {
        if (pageSizeStr != null) {
            try {
                pageSize = Integer.valueOf(pageSizeStr);
            } catch (NumberFormatException e) {
                throw new NumberFormatException("Bad value for: " + PAGE_SIZE_PARAM);
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
        queryParams.add(key, value);
    }

    public List<String> getQueryParam(String key) {
        return queryParams.get(key);
    }

    public MultivaluedMap<String, String> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(MultivaluedMap<String, String> queryParams) {
        this.queryParams = queryParams;
    }
}
