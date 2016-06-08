package org.collectionspace.services.client.test;

import javax.ws.rs.core.Response;

import org.collectionspace.services.client.AuthorityClient;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.PayloadInputPart;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.AbstractCommonListUtils;
import org.collectionspace.services.client.XmlTools;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.dom4j.Document;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;

/*
 * <CLT> - Common list type
 * <CPT> - Common part type
 */
public abstract class AbstractPoxServiceTestImpl<CLT extends AbstractCommonList, CPT>
		extends AbstractServiceTestImpl<CLT, CPT, PoxPayloadOut, String> {
		
	@Override
	public CPT extractCommonPartValue(Response res) throws Exception {
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
        if (getLogger().isDebugEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, getLogger(), testName);
        }
    }
    
    @Override
    protected long getSizeOfList(CLT list) {
    	return list.getTotalItems();    	
    }
    
    /**
     * Extracts the workflow state of PoxPayloadIn
     * 
     * @param res
     * @return
     * @throws Exception
     */
	protected String extractAuthorityWorkflowState(PoxPayloadIn input) throws Exception {
		String result = null;
		
		Document document = input.getDOMDocument();
		result = XmlTools.getElementValue(document, "//" + WorkflowClient.WORKFLOWSTATE_XML_ELEMENT_NAME);

		return result;
	}
    
    /**
     * Extracts the workflow state of a response payload
     * 
     * @param res
     * @return
     * @throws Exception
     */
	protected String extractAuthorityWorkflowState(Response res) throws Exception {
		String result = null;
		
        PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType()));	    	
        result = extractAuthorityWorkflowState(input);
        		
		return result;
	}
    
    /**
     * The entity type expected from the JAX-RS Response object
     */
    public Class<String> getEntityResponseType() {
    	return String.class;
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
	public PoxPayloadOut createRequestTypeInstance(CPT commonPartTypeInstance) throws Exception {
		PoxPayloadOut result = null;
		
		CollectionSpaceClient client = this.getClientInstance();
        PoxPayloadOut payloadOut = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart part = payloadOut.addPart(client.getCommonPartName(), commonPartTypeInstance);
        result = payloadOut;
		
		return result;
	}
    
    protected PayloadInputPart extractPart(Response res, String partLabel)
            throws Exception {
            if (getLogger().isDebugEnabled()) {
            	getLogger().debug("Reading part " + partLabel + " ...");
            }
            PoxPayloadIn input = new PoxPayloadIn((String)res.readEntity(getEntityResponseType()));
            PayloadInputPart payloadInputPart = input.getPart(partLabel);
            Assert.assertNotNull(payloadInputPart,
                    "Part " + partLabel + " was unexpectedly null.");
            return payloadInputPart;
    }
}
