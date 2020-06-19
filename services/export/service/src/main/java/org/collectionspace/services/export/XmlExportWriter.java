package org.collectionspace.services.export;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.dom4j.Element;
import org.dom4j.Namespace;

public class XmlExportWriter extends AbstractExportWriter {
	private int seqNum = 0;

	@Override
	public void start() throws Exception {
		seqNum = 0;

		writer.write("<imports>\n");
	}

	@Override
	public void writeDocument(PoxPayloadOut document) throws Exception {
		String docType = document.getName();
		ServiceBindingType serviceBinding = tenantBindingConfigReader.getServiceBinding(serviceContext.getTenantId(), docType);

		String serviceName = serviceBinding.getName();
		ServiceObjectType objectConfig = serviceBinding.getObject();
		String objectName = objectConfig.getName();

		seqNum += 1;

		writer.write(String.format("<import seq=\"%d\" service=\"%s\" type=\"%s\">\n", seqNum, serviceName, objectName));

		for (PayloadOutputPart part : document.getParts()) {
			String partLabel = part.getLabel();
			Element element = part.asElement();
			Namespace partNamespace = element.getNamespaceForPrefix("ns2");
			String partNamespaceUri = partNamespace.getURI();

			for (Object namespace : element.additionalNamespaces()) {
				element.remove((Namespace) namespace);
			}

			element.remove(partNamespace);

			element.setName("schema");
			element.addAttribute("name", partLabel);
			element.addNamespace(partLabel, partNamespaceUri);

			writer.write(element.asXML());
		}

		writer.write("</import>\n");
	}

	@Override
	public void finish() throws Exception {
		writer.write("</imports>");
	}
}
