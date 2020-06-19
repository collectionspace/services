package org.collectionspace.services.export;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.query.UriInfoImpl;
import org.collectionspace.services.common.vocabulary.AuthorityResource;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.specimpl.PathSegmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDocumentsByQueryIterator<ListItemType> implements Iterator<PoxPayloadOut> {
	private final Logger logger = LoggerFactory.getLogger(AbstractDocumentsByQueryIterator.class);

	private NuxeoBasedResource resource;
	private String vocabulary;
	private boolean isAuthorityItem = false;
	private AbstractCommonList resultList;
	private Iterator<ListItemType> resultItemIterator;
	private InvocationContext.Query query;

	protected ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext;

	AbstractDocumentsByQueryIterator(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String docType,
		String vocabulary,
		InvocationContext.Query query) throws Exception {

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		ServiceBindingType serviceBinding = tenantBindingConfigReader.getServiceBindingForDocType(serviceContext.getTenantId(), docType);
		String serviceType = serviceBinding.getType();
		String serviceName = serviceBinding.getName();

		this.serviceContext = serviceContext;
		this.isAuthorityItem = ServiceBindingUtils.SERVICE_TYPE_AUTHORITY.equals(serviceType);
		this.vocabulary = vocabulary;

		this.resource = isAuthorityItem
			? AuthorityResource.getResourceForItem(serviceContext.getResourceMap(), serviceContext.getTenantId(), docType)
			: (NuxeoBasedResource) serviceContext.getResource(serviceName.toLowerCase());

		this.query = query;

		getResults(query);
	}

	private void getResults(InvocationContext.Query query) throws Exception {
		UriInfo uriInfo = createUriInfo(query);

		resultList = isAuthorityItem
			? ((AuthorityResource<?, ?>) resource).getAuthorityItemList(serviceContext, vocabulary == null ? AuthorityResource.PARENT_WILDCARD : vocabulary, uriInfo)
			: resource.getList(serviceContext, uriInfo);

		resultItemIterator = (resultList == null) ? null : getListItems(resultList).iterator();
	}

	protected abstract List<ListItemType> getListItems(AbstractCommonList list);

	private boolean hasMoreResultPages() {
		if (resultList == null || query.getPgNum() != null) {
			return false;
		}

		long pageSize = resultList.getPageSize();
		long pageNum = resultList.getPageNum();
		long totalItems = resultList.getTotalItems();

		return (totalItems > (pageSize * (pageNum + 1)));
	}

	private void getNextResultPage() throws Exception {
		if (hasMoreResultPages()) {
			long pageSize = resultList.getPageSize();
			long pageNum = resultList.getPageNum();

			InvocationContext.Query nextPageQuery = new InvocationContext.Query();

			nextPageQuery.setAs(query.getAs());
			nextPageQuery.setKw(query.getKw());
			nextPageQuery.setPgNum(BigInteger.valueOf(pageNum + 1));
			nextPageQuery.setPgSz(BigInteger.valueOf(pageSize));
			nextPageQuery.setWfDeleted(query.isWfDeleted());

			getResults(nextPageQuery);
		}
	}

	@Override
	public boolean hasNext() {
		return (
			resultList != null
			&& resultItemIterator != null
			&& (resultItemIterator.hasNext() || hasMoreResultPages())
		);
	}

	@Override
	public PoxPayloadOut next() {
		if (resultList == null || resultItemIterator == null) {
			throw new NoSuchElementException();
		}

		if (!resultItemIterator.hasNext()) {
			if (!hasMoreResultPages()) {
			throw new NoSuchElementException();
			}

			try {
			getNextResultPage();
			}
			catch (Exception e) {
			logger.warn("Could not get result page", e);

			return null;
			}
		}

		return getDocument(resultItemIterator.next());
	}

	protected PoxPayloadOut getDocument(ListItemType item) {
		String csid = getListItemCsid(item);

		try {
			return (isAuthorityItem
			? ((AuthorityResource<?, ?>) resource).getAuthorityItemWithExistingContext(serviceContext, AuthorityResource.PARENT_WILDCARD, csid)
			: resource.getWithParentCtx(serviceContext, csid));
		}
		catch (Exception e) {
			logger.warn("Could not get document with csid " + csid, e);

			return null;
		}
	}

	protected abstract String getListItemCsid(ListItemType listItem);

	protected UriInfo createUriInfo(InvocationContext.Query query) throws URISyntaxException {
		URI	absolutePath = new URI("");
		URI	baseUri = new URI("");
		String encodedPath = "";

		// Some code in services assumes pathSegments will have at least one element, so add an
		// empty one.
		List<PathSegment> pathSegments = Arrays.asList((PathSegment) new PathSegmentImpl("", false));

		URIBuilder uriBuilder = new URIBuilder();

		String as = query.getAs();

		if (StringUtils.isNotEmpty(as)) {
			uriBuilder.addParameter("as", as);
		}

		String kw = query.getKw();

		if (StringUtils.isNotEmpty(kw)) {
			uriBuilder.addParameter("kw", kw);
		}

		BigInteger pgNum = query.getPgNum();

		if (pgNum != null) {
			uriBuilder.addParameter("pgNum", pgNum.toString());
		}

		BigInteger pgSz = query.getPgSz();

		if (pgSz != null) {
			uriBuilder.addParameter("pgSz", pgSz.toString());
		}

		Boolean wfDeleted = query.isWfDeleted();

		if (wfDeleted != null) {
			uriBuilder.addParameter("wf_deleted", Boolean.toString(wfDeleted));
		}

		String sbj = query.getSbj();

		if (StringUtils.isNotEmpty(sbj)) {
			uriBuilder.addParameter("sbj", sbj);
		}

		String sbjType = query.getSbjType();

		if (StringUtils.isNotEmpty(sbjType)) {
			uriBuilder.addParameter("sbjType", sbjType);
		}

		String prd = query.getPrd();

		if (StringUtils.isNotEmpty(prd)) {
			uriBuilder.addParameter("prd", prd);
		}

		String obj = query.getObj();

		if (StringUtils.isNotEmpty(obj)) {
			uriBuilder.addParameter("obj", obj);
		}

		String objType = query.getObjType();

		if (StringUtils.isNotEmpty(objType)) {
			uriBuilder.addParameter("objType", objType);
		}

		Boolean andReciprocal = query.isAndReciprocal();

		if (andReciprocal != null) {
			uriBuilder.addParameter("andReciprocal", Boolean.toString(andReciprocal));
		}

		String queryString = uriBuilder.toString();

		if (StringUtils.isNotEmpty(queryString)) {
			queryString = queryString.substring(1); // Remove ? from beginning
		}

		return new UriInfoImpl(absolutePath, baseUri, encodedPath, queryString, pathSegments);
	}
}
