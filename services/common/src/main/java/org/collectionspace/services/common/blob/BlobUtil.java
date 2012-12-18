package org.collectionspace.services.common.blob;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
//import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlobUtil {
	//
	//
	private static final Logger logger = LoggerFactory.getLogger(BlobUtil.class);
	
    public static BlobInput getBlobInput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	BlobInput result = (BlobInput)ctx.getProperty(BlobInput.class.getName());
    	if (result == null) {
    		result = new BlobInput();
    		setBlobInput(ctx, result);
    	}
    	return result;
	}
    
    public static BlobInput resetBlobInput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx) {
    	BlobInput blobInput = new BlobInput();
    	setBlobInput(ctx, blobInput);
    	return blobInput;
    }
    
    public static void setBlobInput(ServiceContext<PoxPayloadIn, PoxPayloadOut> ctx,
    		BlobInput blobInput) {
    	ctx.setProperty(BlobInput.class.getName(), blobInput);    		
	}	
    
}
