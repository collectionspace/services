package org.collectionspace.services.common;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * 
 * @author remillet
 * 
 * The older versions of RESTEasy allowed us to modify the query parameters passed into us from the UriInfo class.  There
 * are many places in the existing code that rely on changes to the query parameters.  But more recent versions of RESTEasy
 * pass us a read-only copy of the query parameters.  Therefore, this wrapper class allows us to provide the existing code
 * a read-write copy of the query parameters.
 *
 */
public class UriInfoWrapper implements UriInfo {
	//
	// Construct the UriInfoWrapper from a UriInfo instance
	//
	public UriInfoWrapper(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
		// RESTEasy returns a read-only set of query params, so we need to make a read-write copy of them
		if (uriInfo != null) {
			queryParams.putAll(uriInfo.getQueryParameters());
		}
	}
	
	private UriInfo uriInfo;
	private MultivaluedHashMap<String, String> queryParams = new MultivaluedHashMap<String, String>();

	@Override
	public URI getAbsolutePath() {
		return uriInfo.getAbsolutePath();
	}

	@Override
	public UriBuilder getAbsolutePathBuilder() {
		return uriInfo.getAbsolutePathBuilder();
	}

	@Override
	public URI getBaseUri() {
		return uriInfo.getBaseUri();
	}

	@Override
	public UriBuilder getBaseUriBuilder() {
		return uriInfo.getBaseUriBuilder();
	}

	@Override
	public List<Object> getMatchedResources() {
		return uriInfo.getMatchedResources();
	}

	@Override
	public List<String> getMatchedURIs() {
		return uriInfo.getMatchedURIs();
	}

	@Override
	public List<String> getMatchedURIs(boolean arg0) {
		return uriInfo.getMatchedURIs(arg0);
	}

	@Override
	public String getPath() {
		if (uriInfo != null)
			return uriInfo.getPath();
		else
			return null;
	}

	@Override
	public String getPath(boolean arg0) {
		return uriInfo.getPath();
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters() {
		return uriInfo.getPathParameters();
	}

	@Override
	public MultivaluedMap<String, String> getPathParameters(boolean arg0) {
		return uriInfo.getPathParameters(arg0);
	}

	@Override
	public List<PathSegment> getPathSegments() {
		return uriInfo.getPathSegments();
	}

	@Override
	public List<PathSegment> getPathSegments(boolean arg0) {
		return uriInfo.getPathSegments(arg0);
	}

	@Override
	public MultivaluedMap<String, String> getQueryParameters() {
		return this.queryParams;
	}

	/**
	 * Not implemented.
	 * 
	 */
	@Override
	public MultivaluedMap<String, String> getQueryParameters(boolean arg0) {
		throw new java.lang.UnsupportedOperationException();
	}

	@Override
	public URI getRequestUri() {
		return uriInfo.getRequestUri();
	}

	@Override
	public UriBuilder getRequestUriBuilder() {
		return uriInfo.getRequestUriBuilder();
	}

	@Override
	public URI relativize(URI arg0) {
		return uriInfo.relativize(arg0);
	}

	@Override
	public URI resolve(URI arg0) {
		return uriInfo.resolve(arg0);
	}

}
