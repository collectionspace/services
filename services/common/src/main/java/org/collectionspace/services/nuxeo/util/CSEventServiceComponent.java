package org.collectionspace.services.nuxeo.util;

import org.nuxeo.ecm.core.event.EventServiceComponent;
//import org.nuxeo.ecm.core.event.impl.EventServiceImpl;
import org.nuxeo.runtime.model.ComponentContext;

public class CSEventServiceComponent extends EventServiceComponent {
	
    @Override
    public void activate(ComponentContext context) {
        service = new CSEventServiceImpl();
    }

}
