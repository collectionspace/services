package org.collectionspace.services.client;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.testng.Assert;
import org.apache.http.HttpStatus;
import org.collectionspace.services.description.ServiceDescription;
import org.collectionspace.services.jaxb.AbstractCommonList;

/*
 * CLT = List type
 * P = Proxy type
 */
public abstract class AbstractPoxServiceClientImpl<CLT extends AbstractCommonList, P extends CollectionSpacePoxProxy<CLT>, CPT>
	extends AbstractServiceClientImpl<CLT, PoxPayloadOut, String, P> 
	implements CollectionSpacePoxClient<CLT, P> {
	
    protected List<String> allResourceIdsCreated = new ArrayList<String>();
	
    public AbstractPoxServiceClientImpl(String clientPropertiesFilename) throws Exception {
    	super(clientPropertiesFilename);
    }

	public AbstractPoxServiceClientImpl() throws Exception {
		super();
	}
	
	/**
	 * Will delete a set of resources that were explicitly attached to the client.
	 */
	public void cleanup() {
		for (String csid : this.allResourceIdsCreated) {
			this.delete(csid).close();
		}
	}
	
	/**
	 * Adds a CSID to the list of items that should get deleted when cleanup() is called.
	 * @param csid
	 */
	public void addToCleanup(String csid) {
		this.allResourceIdsCreated.add(csid);
	}
	
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
	
	@Override
	public Response create(PoxPayloadOut xmlPayload) {
        return getProxy().create(xmlPayload.getBytes());
    }
		
    @Override
	public Response read(String csid) {
        return getProxy().read(csid);
    }
    
    public Response readList() {
    	CollectionSpaceProxy<CLT> proxy = (CollectionSpaceProxy<CLT>)getProxy();
    	return proxy.readList();
    }    
    
    @Override
    public Response readIncludeDeleted(Boolean includeDeleted) {
    	CollectionSpacePoxProxy<CLT> proxy = getProxy();
    	return proxy.readIncludeDeleted(includeDeleted.toString());
    }
    
    @Override
	public Response readIncludeDeleted(String csid, Boolean includeDeleted) {
        return getProxy().readIncludeDeleted(csid, includeDeleted.toString());
    }

    @Override
    public Response update(String csid, PoxPayloadOut xmlPayload) {
        return getProxy().update(csid, xmlPayload.getBytes());
    }
    

    @Override
    public Response keywordSearchIncludeDeleted(String keywords, Boolean includeDeleted) {
        CollectionSpacePoxProxy<CLT> proxy = getProxy();
        return proxy.keywordSearchIncludeDeleted(keywords, includeDeleted.toString());
    }

    @Override
    public Response advancedSearchIncludeDeleted(String whereClause, Boolean includeDeleted) {
        CollectionSpacePoxProxy<CLT> proxy = getProxy();
        return proxy.advancedSearchIncludeDeleted(whereClause, includeDeleted.toString());
    }
    
    //
    // REM - Attemp to move methods from test framework into Java client framework
    //
    
	public CPT extractCommonPartValue(Response res) throws Exception {
		CPT result = null;
		
		PayloadInputPart payloadInputPart = extractPart(res, this.getCommonPartName());
		if (payloadInputPart != null) {
			result = (CPT) payloadInputPart.getBody();
		}
		
		return result;
	}
	
    protected void printList(String testName, CLT list) {
        if (getLogger().isDebugEnabled()){
        	AbstractCommonListUtils.ListItemsInAbstractCommonList(list, getLogger(), testName);
        }
    }
    
    protected long getSizeOfList(CLT list) {
    	return list.getTotalItems();    	
    }
    
    /**
     * The entity type expected from the JAX-RS Response object
     */
    public Class<String> getEntityResponseType() {
    	return String.class;
    }
	
    public CPT extractCommonPartValue(PoxPayloadOut payloadOut) throws Exception {
    	CPT result = null;
    	
    	PayloadOutputPart payloadOutputPart = payloadOut.getPart(this.getCommonPartName());
    	if (payloadOutputPart != null) {
    		result = (CPT) payloadOutputPart.getBody();
    	}
        
    	return result;
    }
		
	public PoxPayloadOut createRequestTypeInstance(CPT commonPartTypeInstance) {
		PoxPayloadOut result = null;
		
        PoxPayloadOut payloadOut = new PoxPayloadOut(this.getServicePathComponent());
        PayloadOutputPart part = payloadOut.addPart(this.getCommonPartName(), commonPartTypeInstance);
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
