package org.collectionspace.services.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.service.ServiceObjectType;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;

public class CsvExportWriter extends AbstractExportWriter {
	private static final Pattern VALID_FIELD_XPATH_PATTERN = Pattern.compile("^\\w+:(\\w+\\/)*(\\w+)$");
	private static final String AUTH_ITEM_TERM_GROUP_SUFFIX = "TermGroup";

	private CSVPrinter csvPrinter;
	private Map<String, Map<String, Set<String>>> refFieldsByDocType = new HashMap<>();

	@Override
	public void start() throws Exception {
		// For CSV output, the invocation context must specify the exact fields to be included
		// (no wildcard xpaths), so that the columns can be known in advance. Otherwise the entire
		// result set would need to be scanned first, in order to determine the fields that are
		// present.

		InvocationContext.IncludeFields includeFields = invocationContext.getIncludeFields();
		List<String> fields = (includeFields != null ? includeFields.getField() : new ArrayList<String>());

		if (fields.size() == 0) {
			throw new Exception("For CSV output, the fields to export must be specified using includeFields.");
		}

		List<String> headers = new ArrayList<>();

		for (String field : fields) {
			Matcher matcher = VALID_FIELD_XPATH_PATTERN.matcher(field);

			if (!matcher.matches()) {
				throw new Exception("The includeField XPath expression \"" + field + "\" is not valid for CSV output. For CSV output, all included fields must be individually specified without using wildcards.");
			}

			String fieldName = matcher.group(2);

			headers.add(fieldName);

			if (isFieldWithinAuthItemTermGroup(field)) {
				headers.add(fieldName + "NonPreferred");
			}
		}

		String[] headersArray = new String[headers.size()];

		this.csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers.toArray(headersArray)));
	}

	@Override
	public void writeDocument(PoxPayloadOut document) throws Exception {
		InvocationContext.IncludeFields includeFields = invocationContext.getIncludeFields();

		if (includeFields == null) {
			return;
		}

		List<String> fields = includeFields.getField();
		List<String> csvRecord = new ArrayList<>();

		for (String field : fields) {
			if (isFieldWithinAuthItemTermGroup(field)) {
				// Write a column for values within the preferred (primary) term group.
				csvRecord.add(collectValues(document, field.replace(AUTH_ITEM_TERM_GROUP_SUFFIX + "/", AUTH_ITEM_TERM_GROUP_SUFFIX + "[position()=1]/")));

				// Write a column for values within non-preferred term groups.
				csvRecord.add(collectValues(document, field.replace(AUTH_ITEM_TERM_GROUP_SUFFIX + "/", AUTH_ITEM_TERM_GROUP_SUFFIX + "[position()>1]/")));
			}
			else {
				csvRecord.add(collectValues(document, field));
			}
		}

		if (csvRecord.size() > 0) {
			csvPrinter.printRecord(csvRecord);
		}
	}

	@Override
	public void close() throws Exception {
	  csvPrinter.close();
	}

	private boolean isFieldWithinAuthItemTermGroup(String field) {
		// FIXME: How to know a NonPreferred column is needed without hardcoding "TermGroup"?

		return field.contains(AUTH_ITEM_TERM_GROUP_SUFFIX + "/");
	}

	private String collectValues(PoxPayloadOut document, String field) {
		String delimitedValues = "";
		String[] segments = field.split(":", 2);
		String partName = segments[0];
		String xpath = segments[1];

		PayloadOutputPart part = document.getPart(partName);

		if (part != null) {
			delimitedValues = collectValues(document.getName(), partName, part.getElementBody(), Arrays.asList(xpath.split("/")), 0);
		}

		return delimitedValues;
	}

	private String collectValues(String docType, String partName, Element element, List<String> path, int depth) {
		String delimitedValues = "";
		String fieldName = path.get(depth);
		String delimiter = (depth / 2 > 0) ? "^^" : ";";
		List<Node> matches = element.createXPath(fieldName).selectNodes(element);

		if (matches.size() > 0) {
			List<String> values = new ArrayList<>();
			boolean hasValue = false;

			for (Node node : matches) {
				String textValue = "";

				if (depth < path.size() - 1) {
					textValue = collectValues(docType, partName, (Element) node, path, depth + 1);
				}
				else {
					textValue = node.getText();

					boolean isRefName = isRefField(docType, partName, fieldName);

					if (isRefName && StringUtils.isNotEmpty(textValue)) {
						textValue = RefNameUtils.getDisplayName(textValue);
					}
				}

				if (StringUtils.isNotEmpty(textValue)) {
					hasValue = true;
				}

				values.add(textValue);
			}

			if (hasValue) {
				delimitedValues = String.join(delimiter, values);
			}
		}

		return delimitedValues;
	}

	private boolean isRefField(String docType, String partName, String fieldName) {
		return getRefFields(docType, partName).contains(fieldName);
	}

	private Set<String> getRefFields(String docType, String partName) {
		Set<String> refFields = refFieldsByDocType.containsKey(docType)
			? refFieldsByDocType.get(docType).get(partName)
			: null;

		if (refFields != null) {
			return refFields;
		}

		refFields = new HashSet<>();

		ServiceBindingType serviceBinding = tenantBindingConfigReader.getServiceBinding(serviceContext.getTenantId(), docType);

		for (String termRefField : ServiceBindingUtils.getPropertyValuesForPart(serviceBinding, partName, ServiceBindingUtils.TERM_REF_PROP, false)) {
			String[] segments = termRefField.split("[\\/\\|]");
			String fieldName = segments[segments.length - 1];

			refFields.add(fieldName);
		}

		for (String authRefField : ServiceBindingUtils.getPropertyValuesForPart(serviceBinding, partName, ServiceBindingUtils.AUTH_REF_PROP, false)) {
			String[] segments = authRefField.split("[\\/\\|]");
			String fieldName = segments[segments.length - 1];

			refFields.add(fieldName);
		}

		if (!refFieldsByDocType.containsKey(docType)) {
			refFieldsByDocType.put(docType, new HashMap<String, Set<String>>());
		}

		refFieldsByDocType.get(docType).put(partName, refFields);

		return refFields;
	}
}
