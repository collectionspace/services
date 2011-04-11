package org.collectionspace.services.common.document;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;

import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.relation.RelationsCommon;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ValidatorHandlerImpl.
 */
public abstract class ValidatorHandlerImpl<IT, OT> implements ValidatorHandler<IT, OT> {
	
    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(ValidatorHandlerImpl.class);
    
    private ServiceContext<IT, OT> ctx;
    
    protected ServiceContext<IT, OT> getServiceContext() {
    	return ctx;
    }
    
    protected void setServiceContext(ServiceContext<IT, OT> ctx) {
    	this.ctx = ctx;
    }
	
	/* (non-Javadoc)
	 * @see org.collectionspace.services.common.document.ValidatorHandler#validate(org.collectionspace.services.common.document.DocumentHandler.Action, org.collectionspace.services.common.context.ServiceContext)
	 */
	@Override
    public void validate(Action action, ServiceContext<IT, OT> ctx)
    		throws InvalidDocumentException {
		setServiceContext(ctx);
		switch (action) {
			case CREATE:
				handleCreate();
				break;
			case GET:
				handleGet();
				break;
			case GET_ALL:
				handleGetAll();
				break;
			case UPDATE:
				handleUpdate();
				break;
			case DELETE:
				handleDelete();
				break;
			default:
				throw new UnsupportedOperationException("ValidatorHandlerImpl: Unknow action = " +
						action);
		}    	
    }
	
    protected Object getCommonPart() {
    	Object result = null;    	

    	try {
	    	MultipartServiceContext multiPartCtx = (MultipartServiceContext) getServiceContext();
	    	result = multiPartCtx.getInputPart(ctx.getCommonPartLabel(),
	    			getCommonPartClass());
    	} catch (Exception e) {
    		if (logger.isDebugEnabled() == true) {
    			logger.debug("Could not extract common part from multipart input.", e);
    		}
    	}
    	
    	return result;    	
    }	
	
    abstract protected Class<?> getCommonPartClass();
    
	/**
	 * Handle create.
	 *
	 * @param ctx the ctx
	 */
	abstract protected void handleCreate() throws InvalidDocumentException;
	
	/**
	 * Handle get.
	 *
	 * @param ctx the ctx
	 */
	abstract protected void handleGet() throws InvalidDocumentException;
	
	/**
	 * Handle get all.
	 *
	 * @param ctx the ctx
	 */
	abstract protected void handleGetAll() throws InvalidDocumentException;
	
	/**
	 * Handle update.
	 *
	 * @param ctx the ctx
	 */
	abstract protected void handleUpdate() throws InvalidDocumentException;

	/**
	 * Handle delete.
	 *
	 * @param ctx the ctx
	 */
	abstract protected void handleDelete() throws InvalidDocumentException;	
}