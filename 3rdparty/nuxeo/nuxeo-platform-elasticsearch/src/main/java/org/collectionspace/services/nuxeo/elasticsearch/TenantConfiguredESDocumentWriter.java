package org.collectionspace.services.nuxeo.elasticsearch;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import org.apache.commons.lang3.StringUtils;
import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.config.TenantBindingConfigReaderImpl;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.elasticsearch.io.JsonESDocumentWriter;

/**
 * A JsonESDocumentWriter that delegates to the class that is specified in the
 * CSpace tenant binding file for the current tenant.
 */
public class TenantConfiguredESDocumentWriter extends JsonESDocumentWriter {

	@Override
	public void writeESDocument(JsonGenerator jg, DocumentModel doc, String[] schemas,
								Map<String, String> contextParameters) throws IOException {
		String tenantId = (String) doc.getProperty(CollectionSpaceClient.COLLECTIONSPACE_CORE_SCHEMA, CollectionSpaceClient.COLLECTIONSPACE_CORE_TENANTID);

		if (tenantId == null) {
			writeEmptyDoc(jg);
			return;
		}

		TenantBindingConfigReaderImpl tenantBindingConfigReader = ServiceMain.getInstance().getTenantBindingConfigReader();
		TenantBindingType tenantBindingType = tenantBindingConfigReader.getTenantBinding(tenantId);
		ServiceBindingType serviceBinding = tenantBindingConfigReader.getServiceBindingForDocType(tenantId, doc.getType());
		String documentWriterClassName = tenantBindingType.getElasticSearchDocumentWriter();

		if (!serviceBinding.isElasticsearchIndexed() || StringUtils.isBlank(documentWriterClassName)) {
			writeEmptyDoc(jg);
			return;
		}

		documentWriterClassName = documentWriterClassName.trim();

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> documentWriterClass = null;

		try {
			documentWriterClass = loader.loadClass(documentWriterClassName);
		} catch (ClassNotFoundException e) {
			String msg = String.format("Unable to load ES document writer for tenant %s with class %s", tenantId, documentWriterClassName);

			throw new IOException(msg, e);
		}

		if (TenantConfiguredESDocumentWriter.class.equals(documentWriterClass)) {
			String msg = String.format("ES document writer class for tenant %s must not be TenantConfiguredESDocumentWriter", tenantId);

			throw new IOException(msg);
		}

		if (!JsonESDocumentWriter.class.isAssignableFrom(documentWriterClass)) {
			String msg = String.format("ES document writer for tenant %s of class %s is not a subclass of JsonESDocumentWriter", tenantId, documentWriterClassName);

			throw new IOException(msg);
		}

		JsonESDocumentWriter documentWriter = null;

		try {
			documentWriter = (JsonESDocumentWriter) documentWriterClass.newInstance();
		} catch(Exception e) {
			String msg = String.format("Unable to instantiate ES document writer class: %s", documentWriterClassName);

			throw new IOException(msg, e);
		}

		documentWriter.writeESDocument(jg, doc, schemas, contextParameters);
	}

	private void writeEmptyDoc(JsonGenerator jg) throws IOException {
		jg.writeStartObject();
		jg.writeEndObject();
		jg.flush();
	}
}
