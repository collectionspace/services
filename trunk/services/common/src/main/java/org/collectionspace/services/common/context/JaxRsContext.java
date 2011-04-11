package org.collectionspace.services.common.context;

import javax.ws.rs.core.Request;
import javax.ws.rs.core.UriInfo;

public class JaxRsContext {
	private Request request;
	private UriInfo uriInfo;
	
	public JaxRsContext(Request theRequest, UriInfo theUriInfo) {
		this.request = theRequest;
		this.uriInfo = theUriInfo;
	}

	public Request getRequest() {
		return request;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}
}
