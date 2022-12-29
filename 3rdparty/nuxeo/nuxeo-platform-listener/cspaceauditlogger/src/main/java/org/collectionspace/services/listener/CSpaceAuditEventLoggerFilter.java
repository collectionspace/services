package org.collectionspace.services.listener;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.platform.audit.api.AuditLogger;
import org.nuxeo.ecm.platform.audit.listener.AuditEventLogger;
import org.nuxeo.runtime.api.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CSpaceAuditEventLoggerFilter extends AuditEventLogger {

	private static final Logger log = LoggerFactory.getLogger(CSpaceAuditLogger.class);

    @Override
    public boolean acceptEvent(Event event) {
//        AuditLogger logger = Framework.getService(AuditLogger.class);
//        if (logger == null) {
//            return false;
//        }
//        return logger.getAuditableEventNames().contains(event.getName());
    	return false;
    }

    @Override
    public void handleEvent(EventBundle events) {
        AuditLogger logger = Framework.getService(AuditLogger.class);
        if (logger != null) {
            log.error(String.format("Skipping %d entries from the audit log.", events.size()));
            //logger.logEvents(events);
        } else {
            log.error("Can not reach AuditLogger");
        }
    }
}
