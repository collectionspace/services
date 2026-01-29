package org.collectionspace.services.export;

import java.io.OutputStream;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;

public interface ExportWriter {
	void setInvocationContext(InvocationContext invocationContext);
	void setOutputStream(OutputStream outputStream);
	void setServiceContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext);
	void setTenantBindingConfigReader(TenantBindingConfigReaderImpl tenantBindingConfigReader);

	void start() throws Exception;
	void writeDocument(PoxPayloadOut document) throws Exception;
	void finish() throws Exception;
	void close() throws Exception;
}
