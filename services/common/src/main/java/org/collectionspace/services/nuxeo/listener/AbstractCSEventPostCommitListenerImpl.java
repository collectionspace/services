package org.collectionspace.services.nuxeo.listener;

import java.util.Iterator;

import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.PostCommitEventListener;

public abstract class AbstractCSEventPostCommitListenerImpl extends AbstractCSEventListenerImpl implements PostCommitEventListener {

	/**
	 * Subclasses need to override and return 'true' if they want to handle the event bundle
	 * @param eventBundle
	 * @return
	 */
	public abstract boolean shouldHandleEventBundle(EventBundle eventBundle);
	
	/*
	 * Process all the events in the bundle
	 */
	@Override
    public final void handleEvent(EventBundle eventBundle) {
		if (shouldHandleEventBundle(eventBundle)) {
            Iterator<Event> iter = eventBundle.iterator();
            while (iter.hasNext()) {
            	this.handleEvent(iter.next());
            }
        }
    }
}
