package org.collectionspace.services.client.test;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;

/*
 * <CLT> - Common list type
 * <CPT> - Common part type
 */
public abstract class AbstractPoxServiceTestImpl<CLT extends AbstractCommonList, CPT>
		extends AbstractServiceTestImpl<CLT, CPT, PoxPayloadOut, String> {
		
	@Override
	public CPT extractCommonPartValue(ClientResponse<String> res) throws Exception {
		CPT result = null;
		
		CollectionSpaceClient client = getClientInstance();
		PayloadInputPart payloadInputPart = extractPart(res, client.getCommonPartName());
		if (payloadInputPart != null) {
			result = (CPT) payloadInputPart.getBody();
		}
		Assert.assertNotNull(result,
				"Part or body of part " + client.getCommonPartName() + " was unexpectedly null.");
		
		return result;
	}
	
    protected void printList(String testName, CLT list) {
        if (getLogger().isTraceEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, getLogger(), testName);
        }
    }
	
	@Override
    public CPT extractCommonPartValue(PoxPayloadOut payloadOut) throws Exception {
    	CPT result = null;
    	
    	CollectionSpaceClient client = getClientInstance();
    	PayloadOutputPart payloadOutputPart = payloadOut.getPart(client.getCommonPartName());
    	if (payloadOutputPart != null) {
    		result = (CPT) payloadOutputPart.getBody();
    	}
        Assert.assertNotNull(result,
                "Part or body of part " + client.getCommonPartName() + " was unexpectedly null.");
        
    	return result;
    }	
	
	@Override
	public PoxPayloadOut createRequestTypeInstance(CPT commonPartTypeInstance) {
		PoxPayloadOut result = null;
		
		CollectionSpaceClient client = this.getClientInstance();
        PoxPayloadOut payloadOut = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart part = payloadOut.addPart(client.getCommonPartName(), commonPartTypeInstance);
        result = payloadOut;
		
		return result;
	}
    
    protected PayloadInputPart extractPart(ClientResponse<String> res, String partLabel)
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
