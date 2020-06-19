/**
 * ExportClient.java
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
import org.collectionspace.services.export.ExportsCommon;

/**
 * An Export client.

 * @version $Revision:$
 */
public class ExportClient extends AbstractCommonListPoxServiceClientImpl<ExportProxy, ExportsCommon> {
	public static final String SERVICE_NAME = "exports";
	public static final String SERVICE_PATH_COMPONENT = SERVICE_NAME;
	public static final String SERVICE_PATH = "/" + SERVICE_PATH_COMPONENT;
	public static final String SERVICE_COMMON_PART_NAME = SERVICE_NAME + PART_LABEL_SEPARATOR + PART_COMMON_LABEL;

	public ExportClient() throws Exception {
		super();
	}

	public ExportClient(String clientPropertiesFilename) throws Exception {
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
	public Class<ExportProxy> getProxyClass() {
		return ExportProxy.class;
	}
}
