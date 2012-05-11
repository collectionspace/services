package org.collectionspace.services.common.document;

import org.collectionspace.services.common.context.MultipartServiceContext;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * The Class ValidatorHandlerImpl.
 */
public abstract class ValidatorHandlerImpl<IT, OT> implements ValidatorHandler<IT, OT> {

    /**
     * The logger.
     */
    private final Logger logger = LoggerFactory.getLogger(ValidatorHandlerImpl.class);
    private ServiceContext<IT, OT> ctx;

    protected ServiceContext<IT, OT> getServiceContext() {
        return ctx;
    }
    // gets reset by calls to setServiceContext() method
    protected boolean enforceAsserts = true;

    public boolean getEnforceAsserts() {
        return enforceAsserts;
    }

    public void setEnforceAsserts(ServiceContext<IT, OT> ctx) {
        Boolean disableAssertsAttr = ctx.getServiceBinding().isDisableAsserts();
        if (disableAssertsAttr == null) {
            enforceAsserts = true;
        } else {
            enforceAsserts = !disableAssertsAttr.booleanValue();
        }
    }

    protected void setServiceContext(ServiceContext<IT, OT> ctx) {
        this.ctx = ctx;

    }

    protected void CS_ASSERT(boolean expression, String errorMsg) throws AssertionError {
        if (expression != true) {
            if (errorMsg == null) {
                errorMsg = "Validation exception occurred in: "
                        + this.getClass().getName();
            }
            throw new AssertionError(errorMsg);
        }
    }

    protected void CS_ASSERT(boolean expression) throws AssertionError {
        CS_ASSERT(expression, null);
    }

    private void init(ServiceContext<IT, OT> ctx) {
        setEnforceAsserts(ctx);
        setServiceContext(ctx);
    }

    /*
     * (non-Javadoc) @see
     * org.collectionspace.services.common.document.ValidatorHandler#validate(org.collectionspace.services.common.document.DocumentHandler.Action,
     * org.collectionspace.services.common.context.ServiceContext)
     */
    @Override
    public void validate(Action action, ServiceContext<IT, OT> ctx)
            throws InvalidDocumentException {
        init(ctx);

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
                throw new UnsupportedOperationException("ValidatorHandlerImpl: Unknown action = "
                        + action);
        }
    }

    protected boolean enforceAsserts() {
        return !ctx.getServiceBinding().isDisableAsserts();
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