package org.collectionspace.services.nuxeo.util;

import java.io.Serializable;
import java.util.Map;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.EventServiceImpl;

public class CSEventServiceImpl extends EventServiceImpl {
	
	@Override
    public void fireEvent(Event event) {
		String repoName = event.getContext().getRepositoryName();
		Map<String, Serializable> eventProps = event.getContext().getProperties();
		super.fireEvent(event);
	}

}
