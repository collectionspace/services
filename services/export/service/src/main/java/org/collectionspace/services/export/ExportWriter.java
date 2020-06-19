package org.collectionspace.services.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;

public interface ExportWriter {
	public void setInvocationContext(InvocationContext invocationContext);
	public void setOutputStream(OutputStream outputStream);
	public void setServiceContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext);
	public void setTenantBindingConfigReader(TenantBindingConfigReaderImpl tenantBindingConfigReader);

	public void start() throws Exception;
	public void writeDocument(PoxPayloadOut document) throws Exception;
	public void finish() throws Exception;
	public void close() throws Exception;
}
