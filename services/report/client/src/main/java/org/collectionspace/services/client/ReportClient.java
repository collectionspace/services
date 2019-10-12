/**	
 * ReportClient.java
 *
 * {Purpose of This Class}
 *
 * {Other Notes Relating to This Class (Optional)}
 *
 * $LastChangedBy: $
 * $LastChangedRevision: $
 * $LastChangedDate: $
 *
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 {Contributing Institution}
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.report.ReportsCommon;

/**
 * A ReportClient.

 * @version $Revision:$
 * FIXME: http://issues.collectionspace.org/browse/CSPACE-1684
 */
public class ReportClient extends AbstractCommonListPoxServiceClientImpl<ReportProxy, ReportsCommon> {

    public static final String SERVICE_NAME = "reports";
    public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
    public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;
    public static final String PDF_MIME_TYPE = "application/pdf";
    public static final String CSV_MIME_TYPE = "text/csv";
    public static final String TSV_MIME_TYPE = "text/tab-separated-values";
    public static final String MSWORD_MIME_TYPE = "application/msword";
    public static final String OPEN_DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String MSEXCEL_MIME_TYPE = "application/vnd.ms-excel";
    public static final String OPEN_XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String MSPPT_MIME_TYPE = "application/vnd.ms-powerpoint";
    public static final String OPEN_PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
    public static final String DEFAULT_REPORT_OUTPUT_MIME = PDF_MIME_TYPE;
    public static final String COMPILED_REPORT_EXTENSION = ".jasper";
    public static final String REPORT_DECSRIPTION_EXTENSION = ".jrxml";

    public ReportClient() throws Exception {
		super();
	}

    public ReportClient(String clientPropertiesFilename) throws Exception {
		super(clientPropertiesFilename);
	}

	@Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
	public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

	@Override
	public Class<ReportProxy> getProxyClass() {
		return ReportProxy.class;
	}
	
	/*
	 * Proxied service calls
	 */
	
    /**
     * @return
     * @see org.collectionspace.services.client.ReportProxy#getReport()
     */
    public Response readListFiltered(
        		String docType, String mode) {
        return getProxy().readListFiltered(docType, mode);
    }
    
    public Response publishReport(String csid,
    		InvocationContext invContext) {
    	return getProxy().publishReport(csid, invContext);
    }
    
}
