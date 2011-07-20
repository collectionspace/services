package org.collectionspace.services.client;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;

public abstract class AbstractCommonListPoxServiceClientImpl<P 
		extends CollectionSpaceCommonListPoxProxy> extends
		AbstractPoxServiceClientImpl<AbstractCommonList, P> {

	/*
	 * Proxied service calls.
	 */
	
    /**
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake()
     */
    public ClientResponse<AbstractCommonList> readList() {
        return getProxy().readList();
    }

    /**
     * @param csid
     * @return
     * @see org.collectionspace.hello.client.IntakeProxy#getIntake(java.lang.String)
     */
    @Override
	public ClientResponse<String> read(String csid) {
        return getProxy().read(csid);
    }
}
