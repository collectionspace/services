package org.collectionspace.services.export;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;

public abstract class AbstractExportWriter implements ExportWriter {
  protected InvocationContext invocationContext;
  protected ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext;
  protected TenantBindingConfigReaderImpl tenantBindingConfigReader;
  protected Writer writer;

  @Override
  public void setInvocationContext(InvocationContext invocationContext) {
    this.invocationContext = invocationContext;
  }

  @Override
  public void setOutputStream(OutputStream outputStream) {
    this.writer = new OutputStreamWriter(outputStream);
  }

  @Override
  public void setServiceContext(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext) {
    this.serviceContext = serviceContext;
  }

  @Override
  public void setTenantBindingConfigReader(TenantBindingConfigReaderImpl tenantBindingConfigReader) {
    this.tenantBindingConfigReader = tenantBindingConfigReader;
  }

  @Override
  public void start() throws Exception {
  };

  @Override
  public abstract void writeDocument(PoxPayloadOut document) throws Exception;

  @Override
  public void finish() throws Exception {
  }

  @Override
  public void close() throws Exception {
    writer.close();
  }
}
