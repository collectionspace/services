/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.collectionspace.services.client.ExportClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.common.AbstractCollectionSpaceResourceImpl;
import org.collectionspace.services.common.ServiceMessages;
import org.collectionspace.services.common.context.MultipartServiceContextFactory;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.context.ServiceContextFactory;
import org.collectionspace.services.common.invocable.Field;
import org.collectionspace.services.common.invocable.Invocable;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path(ExportClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class ExportResource extends AbstractCollectionSpaceResourceImpl<PoxPayloadIn, PoxPayloadOut> {
	private final Logger logger = LoggerFactory.getLogger(ExportResource.class);

	// There is no way to tell from config that collectionspace_core should not be exported, so it
	// has to be hardcoded here. At that point we might as well also hardcode account_permission,
	// so we don't need to look at config at all.

	private static final List<String> EXCLUDE_PARTS = Arrays.asList("collectionspace_core", "account_permission");

	private static final String MIME_TYPE_CSV = "text/csv";
	private static final String MIME_TYPE_XML = "application/xml";
	private static final String INCLUDE_ATTRIBUTE_NAME = "cspace-export-include";

	@Override
	protected String getVersionString() {
		final String lastChangeRevision = "$LastChangedRevision: 1982 $";
		return lastChangeRevision;
	}

	@Override
	public String getServiceName() {
		return ExportClient.SERVICE_NAME;
	}

	@Override
	public Class<?> getCommonPartClass() {
		return ExportsCommon.class;
	}

	@Override
	public ServiceContextFactory<PoxPayloadIn, PoxPayloadOut> getServiceContextFactory() {
		return MultipartServiceContextFactory.get();
	}

	@GET
	@Produces("text/html")
	public String getInputForm() {
		return "<html><head></head><body>Export</body></html>";
	}

	@POST
	public Response invokeExport(@Context UriInfo uriInfo, InvocationContext invocationContext) throws Exception {
		String outputMimeType = getOutputMimeType(invocationContext);

		if (!(outputMimeType.equals(MIME_TYPE_XML) || outputMimeType.equals(MIME_TYPE_CSV))) {
			throw new Exception("Unsupported output MIME type " + outputMimeType);
		}

		String outputFileName = "export." + (outputMimeType.equals(MIME_TYPE_XML) ? "xml" : "csv");

		try {
			ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext = createServiceContext();
			InputStream exportStream = invokeExport(serviceContext, invocationContext);

			return Response.ok(exportStream, outputMimeType)
					.header("Content-Disposition", "inline;filename=\"" + outputFileName + "\"").build();
		} catch (Exception e) {
			String message = e.getMessage();

			throw bigReThrow(e, ServiceMessages.POST_FAILED + (message != null ? message : ""));
		}
	}

	private String getOutputMimeType(InvocationContext invocationContext) {
		String outputMimeType = invocationContext.getOutputMIME();

		if (StringUtils.isEmpty(outputMimeType)) {
			outputMimeType = MIME_TYPE_XML;
		}

		return outputMimeType;
	}

	private InputStream invokeExport(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			InvocationContext invocationContext) throws Exception {

		Iterator<PoxPayloadOut> documents = getDocuments(serviceContext, invocationContext);

		return exportDocuments(serviceContext, invocationContext, documents);
	}

	private InputStream exportDocuments(ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
			InvocationContext invocationContext, Iterator<PoxPayloadOut> documents) throws Exception {

		File exportFile = File.createTempFile("export-", null);
		FileOutputStream outputStream = new FileOutputStream(exportFile);
		ExportWriter exportWriter = getExportWriter(serviceContext, invocationContext, outputStream);

		exportWriter.start();

		while (documents.hasNext()) {
			PoxPayloadOut document = documents.next();

			if (document != null) {
				filterFields(document, invocationContext);

				exportWriter.writeDocument(document);
			}
		}

		exportWriter.finish();
		exportWriter.close();

		return new FileInputStream(exportFile);
	}

	private void filterFields(PoxPayloadOut document, InvocationContext invocationContext) {
		if (document == null) {
			return;
		}

		for (String partName : EXCLUDE_PARTS) {
			PayloadOutputPart part = document.getPart(partName);

			if (part != null) {
				document.removePart(part);
			}
		}

		InvocationContext.ExcludeFields excludeFields = invocationContext.getExcludeFields();

		if (excludeFields != null) {
			List<Field> fields = excludeFields.getField();

			for (Field field : fields) {
				String fieldSpec = field.getValue();
				String[] segments = fieldSpec.split(":", 2);

				String partName = segments[0];
				String xpath = segments[1];

				PayloadOutputPart part = document.getPart(partName);

				if (part != null) {
					org.dom4j.Element partElement = part.getElementBody();
					List<Node> matches = (List<Node>) partElement.selectNodes(xpath);

					for (Node excludeNode : matches) {
						if (excludeNode.getNodeType() == Node.ELEMENT_NODE) {
							excludeNode.detach();
						}
					}
				}
			}
		}

		InvocationContext.IncludeFields includeFields = invocationContext.getIncludeFields();

		if (includeFields != null) {
			List<Field> fields = includeFields.getField();

			for (Field field : fields) {
				String fieldSpec = field.getValue();
				String[] segments = fieldSpec.split(":", 2);

				String partName = segments[0];
				String xpath = segments[1];

				PayloadOutputPart part = document.getPart(partName);

				if (part != null) {
					org.dom4j.Element partElement = part.getElementBody();
					List<Node> matches = (List<Node>) partElement.selectNodes(xpath);

					for (Node includeNode : matches) {
						if (includeNode.getNodeType() == Node.ELEMENT_NODE) {
							markIncluded((org.dom4j.Element) includeNode);
						}
					}
				}
			}

			ArrayList<PayloadOutputPart> includedParts = new ArrayList<>();

			for (PayloadOutputPart part : document.getParts()) {
				org.dom4j.Element partElement = part.getElementBody();

				if (partElement.attributeValue(INCLUDE_ATTRIBUTE_NAME) != null) {
					includedParts.add(part);
					removeUnincluded(partElement);
				}
			}

			document.setParts(includedParts);
		}
	}

	private void markIncluded(org.dom4j.Element element) {
		org.dom4j.Element parentElement = element.getParent();

		if (parentElement != null) {
			markIncluded(parentElement);
		}

		element.addAttribute(INCLUDE_ATTRIBUTE_NAME, "1");
	}

	private void removeUnincluded(org.dom4j.Element element) {
		if (element.attributeValue(INCLUDE_ATTRIBUTE_NAME) == null) {
			element.detach();
		}
		else {
			element.addAttribute(INCLUDE_ATTRIBUTE_NAME, null);

			Iterator childIterator = element.elementIterator();

			while (childIterator.hasNext()) {
				org.dom4j.Element childElement = (org.dom4j.Element) childIterator.next();

				removeUnincluded(childElement);
			}
		}
	}

	private ExportWriter getExportWriter(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		InvocationContext invocationContext,
		OutputStream outputStream) {

		String outputMimeType = getOutputMimeType(invocationContext);
		AbstractExportWriter exportWriter = null;

		if (outputMimeType.equals(MIME_TYPE_XML)) {
			exportWriter = new XmlExportWriter();
		}
		else if (outputMimeType.equals(MIME_TYPE_CSV)) {
			exportWriter = new CsvExportWriter();
		}

		if (exportWriter != null) {
			exportWriter.setInvocationContext(invocationContext);
			exportWriter.setOutputStream(outputStream);
			exportWriter.setServiceContext(serviceContext);
			exportWriter.setTenantBindingConfigReader(getTenantBindingsReader());
		}

		return exportWriter;
	}

	private Iterator<PoxPayloadOut> getDocuments(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		InvocationContext invocationContext) throws Exception {

		String targetDocType = invocationContext.getDocType();
		String targetVocabulary = invocationContext.getVocabulary();

		switch (invocationContext.getMode().toLowerCase()) {
			case Invocable.INVOCATION_MODE_QUERY:
				return getDocumentsByQuery(serviceContext, targetDocType, targetVocabulary, invocationContext.getQuery());
			case Invocable.INVOCATION_MODE_SINGLE:
				return getDocumentByCsid(serviceContext, targetDocType, targetVocabulary, invocationContext.getSingleCSID());
			case Invocable.INVOCATION_MODE_LIST:
				return getDocumentsByCsid(serviceContext, targetDocType, targetVocabulary, invocationContext.getListCSIDs().getCsid());
			case Invocable.INVOCATION_MODE_GROUP:
				return getDocumentsByGroup(serviceContext, invocationContext.getGroupCSID());
			case Invocable.INVOCATION_MODE_NO_CONTEXT:
				return getDocumentsByType(serviceContext, targetDocType, targetVocabulary);
			default:
				throw new UnsupportedOperationException("Unsupported invocation mode: " + invocationContext.getMode());
		}
	}

	private Iterator<PoxPayloadOut> getDocumentsByType(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String docType,
		String vocabulary) throws Exception {

		return getDocumentsByQuery(serviceContext, docType, vocabulary, new InvocationContext.Query());
	}

	private Iterator<PoxPayloadOut> getDocumentsByQuery(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String docType,
		String vocabulary,
		InvocationContext.Query query) throws Exception {

		if (RelationClient.SERVICE_DOC_TYPE.equals(docType)) {
			return new RelationsByQueryIterator(serviceContext, docType, vocabulary, query);
		}

		return new StandardDocumentsByQueryIterator(serviceContext, docType, vocabulary, query);
	}

	private Iterator<PoxPayloadOut> getDocumentByCsid(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String docType,
		String vocabulary,
		String csid) throws Exception {

		return getDocumentsByCsid(serviceContext, docType, vocabulary, Arrays.asList(csid));
	}

	private Iterator<PoxPayloadOut> getDocumentsByCsid(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String docType,
		String vocabulary,
		List<String> csids) throws Exception {

		return new DocumentsByCsidIterator(serviceContext, docType, vocabulary, csids);
	}

	private Iterator<PoxPayloadOut> getDocumentsByGroup(
		ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
		String csid) throws Exception {

		return new DocumentsByGroupIterator(serviceContext, csid);
	}
}
