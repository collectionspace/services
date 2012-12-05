package org.collectionspace.services.listener;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.collectionspace.services.movement.nuxeo.MovementConstants;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.impl.LifeCycleFilter;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

public class UpdateObjectLocationOnMove implements EventListener {

    // FIXME: We might experiment here with using log4j instead of Apache Commons Logging;
    // am using the latter to follow Ray's pattern for now
    final Log logger = LogFactory.getLog(UpdateObjectLocationOnMove.class);

    @Override
    public void handleEvent(Event event) throws ClientException {
        
        logger.info("In handleEvent in UpdateObjectLocationOnMove ...");

        EventContext eventContext = event.getContext();
        if (eventContext == null) {
            return;
        }
        DocumentEventContext docEventContext = (DocumentEventContext) eventContext;
        DocumentModel docModel = docEventContext.getSourceDocument();
        logger.debug("docType=" + docModel.getType());
        if (docModel.getType().startsWith(MovementConstants.NUXEO_DOCTYPE))  {
            logger.info("A create or update event for a Movement document was received by UpdateObjectLocationOnMove ...");
        }
    }
}
