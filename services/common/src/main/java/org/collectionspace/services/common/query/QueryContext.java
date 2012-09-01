package org.collectionspace.services.common.query;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.document.DocumentNotFoundException;

import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;

// TODO: Auto-generated Javadoc
/**
 * The Class QueryContext.
 */
public class QueryContext {

    /** The doc type. */
    String docType;
    /** The doc filter. */
    DocumentFilter docFilter;
    /** The where clause. */
    String whereClause;
    /** The order by clause. */
    String orderByClause;
    /** The domain. */
    String domain;
    /** The tenant id. */
    String tenantId;

    static public final String getTenantQualifiedDoctype(QueryContext queryContext, String docType) {
    	return docType + ServiceContext.TENANT_SUFFIX + queryContext.getTenantId();
    }
    
    /**
     * Instantiates a new query context.
     *
     * @param ctx the ctx
     * @throws DocumentNotFoundException the document not found exception
     * @throws DocumentException the document exception
     */
    QueryContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) throws DocumentNotFoundException, DocumentException {
        docType = ctx.getDocumentType();
        if (docType == null) {
            throw new DocumentNotFoundException(
                    "Unable to find DocumentType for service " + ctx.getServiceName());
        }
        domain = ctx.getRepositoryDomainStorageName();
        tenantId = ctx.getTenantId();
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Service context has no Tenant ID specified.");
        }
    }

    /**
     * Instantiates a new query context.
     *
     * @param ctx the ctx
     * @param theWhereClause the where clause
     * @throws DocumentNotFoundException the document not found exception
     * @throws DocumentException the document exception
     */
    public QueryContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String theWhereClause) throws DocumentNotFoundException, DocumentException {
        this(ctx);
        whereClause = theWhereClause;
    }
    
    /**
     * Instantiates a new query context.
     *
     * @param ctx the ctx
     * @param theWhereClause the where clause
     * @param theOrderByClause the order by clause
     * @throws DocumentNotFoundException the document not found exception
     * @throws DocumentException the document exception
     */
    public QueryContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            String theWhereClause, String theOrderByClause) throws DocumentNotFoundException, DocumentException {
        this(ctx);
        whereClause = theWhereClause;
        orderByClause = theOrderByClause;
    }

    /**
     * Instantiates a new query context.
     *
     * @param ctx the ctx
     * @param handler the handler
     * @throws DocumentNotFoundException the document not found exception
     * @throws DocumentException the document exception
     */
    public QueryContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
            DocumentHandler handler) throws DocumentNotFoundException, DocumentException {
        this(ctx);
        if (handler == null) {
            throw new IllegalArgumentException(
                    "Document handler is missing.");
        }
        docFilter = handler.getDocumentFilter();
        if (docFilter == null) {
            throw new IllegalArgumentException(
                    "Document handler has no Filter specified.");
        }
        whereClause = docFilter.getWhereClause();
        orderByClause = docFilter.getOrderByClause();
    }
    
    /**
     * Gets the doc filter.
     *
     * @return the doc filter
     */
    public DocumentFilter getDocFilter() {
    	return docFilter;
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
     * Gets the tenant id.
     *
     * @return the tenant id
     */
    public String getTenantId() {
    	return this.tenantId;
    }
    
    /**
     * Gets the order by clause.
     *
     * @return the order by clause
     */
    public String getOrderByClause() {
    	return this.orderByClause;
    }
    
    /**
     * Gets the doc type.
     *
     * @return the doc type
     */
    public String getDocType() {
    	return this.docType;
    }
    
    public final String getTenantQualifiedDoctype() {
    	return QueryContext.getTenantQualifiedDoctype(this, docType);
    }    

    /**
     * Gets the doc type.
     *
     * @return the doc type
     */
    public void setDocType(String theDocType) {
    	this.docType = theDocType;
    }
}
