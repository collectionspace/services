package org.collectionspace.services.nuxeo.extension.listener.relation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class RelationSubOrObjDeletionListener implements EventListener {
    
    // FIXME Experiment to identify whether we can use log4j here instead of Apache Commons Logging
    final Log logger = LogFactory.getLog(RelationSubOrObjDeletionListener.class);

    public void handleEvent(Event event) throws ClientException {
        logger.debug("In handleEvent in RelationSubOrObjDeletionListener ...");
    }
}
