/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */

package org.collectionspace.services.client;

/**
 * IClientQueryParams.java
 *
 * Specifies contants used as query parameters in client requests to services.
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 */

public interface IClientQueryParams {

    public static final String PAGE_SIZE_PARAM = "pgSz";
    public static final String START_PAGE_PARAM = "pgNum";
    public static final String ORDER_BY_PARAM = "sortBy";
    public static final String IMPORT_TIMEOUT_PARAM = "impTimout";
    
}
