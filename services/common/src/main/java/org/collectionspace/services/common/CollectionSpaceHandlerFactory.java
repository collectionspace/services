package org.collectionspace.services.common;

import org.collectionspace.services.common.repository.DocumentHandler;
import org.collectionspace.services.common.NuxeoClientType;

public interface CollectionSpaceHandlerFactory {
    public DocumentHandler getHandler(String clientType) throws IllegalArgumentException;
}
