package org.collectionspace.services.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import org.collectionspace.services.client.Profiler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class BlobProfileAspect {
	private static final Logger logger = LoggerFactory.getLogger(BlobProfileAspect.class);
	private static int indent = 0;
	
	private int pushIndent() {
		return indent++;
	}
	
	private int popIndent() {
		return --indent;
	}


	@Around("blobResourceCreateMethods()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		Object result = null;
		
		// Start profiler
        Profiler profiler = new Profiler(pjp.getSignature().toShortString(), pushIndent());
        profiler.start();

        try {
        		Object[] args = pjp.getArgs(); // gets us a read-only copy of the argument(s)
            result = pjp.proceed();	// finish the call
        } finally {
            // No cleanup needed.
        }
        
        profiler.stop();
        popIndent();
        
        return result;
    }

	/**
	 * Setup a pointcut for all the Blob methods related to creating a new blob
	 */
//	@Pointcut("execution(org.nuxeo.ecm.platform.filemanager.service.FileManagerService getFileManagerService(..))")
//    public void nuxeoImagePluginCutPoint() {}
	
	@Pointcut("execution(* org.collectionspace.services.common.imaging.nuxeo.NuxeoBlobUtils.createDocumentFromBlob(..))")
    public void createDocumentFromBlobCutPoint() {}
	
    @Pointcut("execution(* org.collectionspace.services.blob.BlobResource.create(..))")
    public void blobResourceCreateCutPoint() {}
    
    @Pointcut("execution(* org.collectionspace.services.blob.BlobResource.createBlob(..))")
    public void blobResourceCreatBlobCutPoint() {}
    
    @Pointcut("execution(void org.collectionspace.services.common.blob.BlobInput.createBlobFile(..))")
    public void blobUtilCreatBlobFile() {}
    
    @Pointcut("createDocumentFromBlobCutPoint()"
    		+ " || blobResourceCreateCutPoint()"
    		+ " || blobResourceCreatBlobCutPoint()"
    		+ " || blobUtilCreatBlobFile()")
//    		+ " || nuxeoImagePluginCutPoint()")
    public void blobResourceCreateMethods() {}

}
