package org.collectionspace.services.client.test;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.CollectionSpaceCommonListPoxProxy;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;

/*
 * CPT - Common Part Type
 */
public abstract class AbstractGenericServiceTestImpl<CPT> extends AbstractServiceTestImpl {
	public CPT getCommonTypeInstance() {
		CPT result = null;
		return result;
	}
	
    public CPT extractCommonPartValue(ClientResponse<String> res)
            throws Exception {
    		CollectionSpaceClient<AbstractCommonList, CollectionSpaceCommonListPoxProxy> client = this.getClientInstance();
            PayloadInputPart payloadInputPart = extractPart(res, client.getCommonPartName());
            Object obj = null;
            if (payloadInputPart != null) {
            	obj = payloadInputPart.getBody();
            }
            Assert.assertNotNull(obj,
                    "Body of " + client.getCommonPartName() + " part was unexpectedly null.");
            CPT commonPartTypeInstance = (CPT) obj;
            Assert.assertNotNull(commonPartTypeInstance,
                    client.getCommonPartName() + " part was unexpectedly null.");
            return commonPartTypeInstance;
        }
    
    private PayloadInputPart extractPart(ClientResponse<String> res, String partLabel)
            throws Exception {
            if (getLogger().isDebugEnabled()) {
            	getLogger().debug("Reading part " + partLabel + " ...");
            }
            PoxPayloadIn input = new PoxPayloadIn(res.getEntity());
            PayloadInputPart payloadInputPart = input.getPart(partLabel);
            Assert.assertNotNull(payloadInputPart,
                    "Part " + partLabel + " was unexpectedly null.");
            return payloadInputPart;
        }
    
	
}
