package org.collectionspace.services.export;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.context.ServiceBindingUtils;
import org.collectionspace.services.common.invocable.Field;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.config.service.AuthorityInstanceType;
import org.collectionspace.services.config.service.ServiceBindingType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.dom4j.Element;
import org.dom4j.Node;

public class CsvExportWriter extends AbstractExportWriter {
	private static final Pattern VALID_FIELD_XPATH_PATTERN = Pattern.compile("^\\w+:(\\w+/)*(\\w+)(\\[.*])?$");
	private static final String VALUE_DELIMITER_PARAM_NAME = "valuedelimiter";
	private static final String VALUE_EXPORT_AUTH_HEADER_NAME = "authexport";

	private CSVPrinter csvPrinter;
	private Map<String, Map<String, Set<String>>> refFieldsByDocType = new HashMap<>();
	private String valueDelimiter = "|";
	private String nestedValueDelimiter = "^^";
	private AuthorityVocabExport authorityExportType = AuthorityVocabExport.DEFAULT;

	/**
	 * Authority names in the service bindings start with a single capital letter, e.g. Personauthorities, but in
	 * refnames they are all lowercase. Instead of using toLower just set case-insensitive.
	 */
	private Map<String, AuthorityDisplayMapping> authorityDisplayNames = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Override
	public void start() throws Exception {
		// For CSV output, the invocation context must specify the exact fields to be included
		// (no wildcard xpaths), so that the columns can be known in advance. Otherwise the entire
		// result set would need to be scanned first, in order to determine the fields that are
		// present.

		InvocationContext.IncludeFields includeFields = invocationContext.getIncludeFields();
		List<Field> fields = (includeFields != null ? includeFields.getField() : new ArrayList<>());

		if (fields.size() == 0) {
			throw new Exception("For CSV output, the fields to export must be specified using includeFields.");
		}

		// Set the export parameters.
		InvocationContext.Params params = invocationContext.getParams();
		if (params != null) {
			for (InvocationContext.Params.Param param : params.getParam()) {
				if (param.getKey().equals(VALUE_DELIMITER_PARAM_NAME)) {
					this.valueDelimiter = param.getValue();
				} else if (param.getKey().equals(VALUE_EXPORT_AUTH_HEADER_NAME)) {
					this.authorityExportType = AuthorityVocabExport.fromString(param.getValue());
					collectAuthorityVocabs();
				}
			}
		}

		List<String> headers = new ArrayList<>();

		for (Field field : fields) {
			String fieldSpec = field.getValue();
			Matcher matcher = VALID_FIELD_XPATH_PATTERN.matcher(fieldSpec);

			if (!matcher.matches()) {
				throw new Exception("The includeField expression \"" + fieldSpec + "\" is not valid for CSV output. For CSV output, all included fields must be individually specified without using wildcards.");
			}

			String fieldName = field.getName();

			if (fieldName == null) {
				fieldName = matcher.group(2);
			}

			headers.add(fieldName);

			if (authorityExportType == AuthorityVocabExport.COMBINED) {
				// maybe introduce a way to get a doc type from a part name? Or embed as part of the Field?
				// would need to pull the Field out of an xsd format though
				String partName = fieldSpec.split(":", 2)[0];
				String docType = partName.substring(0, partName.indexOf("_"));
				if (isRefField(docType, partName, fieldName)) {
					// make constant
					headers.add(fieldName + "AuthorityVocabulary");
				}
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

		List<Field> fields = includeFields.getField();
		List<String> csvRecord = new ArrayList<>();

		for (Field field : fields) {
			boolean isRefName = isRefField(document, field);

			csvRecord.add(collectValues(document, field.getValue(), isRefName));
			if (isRefName && authorityExportType == AuthorityVocabExport.COMBINED) {
				csvRecord.add(collectAuthorityRefs(document, field.getValue()));
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

	private String collectValues(PoxPayloadOut document, String fieldSpec, boolean isRefField) {
		String delimitedValues = "";
		String[] segments = fieldSpec.split(":", 2);
		String partName = segments[0];
		String xpath = segments[1];

		PayloadOutputPart part = document.getPart(partName);

		if (part != null) {
			delimitedValues = collectValues(part.getElementBody(), Arrays.asList(xpath.split("/")), 0, isRefField);
		}

		return delimitedValues;
	}

	private String collectAuthorityRefs(PoxPayloadOut document, String fieldSpec) {
		String delimitedValues = "";
		String[] segments = fieldSpec.split(":", 2);
		String partName = segments[0];
		String xpath = segments[1];

		PayloadOutputPart part = document.getPart(partName);

		if (part != null) {
			delimitedValues = collectAuthorityRefs(part.getElementBody(), Arrays.asList(xpath.split("/")), 0);
		}

		return delimitedValues;
	}

	private String collectAuthorityRefs(Element element,
	                                    List<String> path,
	                                    int depth) {
		String delimitedValues = "";
		String fieldName = path.get(depth);
		String delimiter = (depth / 2 > 0) ? this.nestedValueDelimiter : this.valueDelimiter;
		List<Node> matches = element.selectNodes(fieldName);

		if (matches.size() > 0) {
			List<String> values = new ArrayList<>();
			boolean hasValue = false;

			for (Node node : matches) {
				String textValue;

				if (depth < path.size() - 1) {
					textValue = collectAuthorityRefs((Element) node, path, depth + 1);
				} else {
					textValue = node.getText();

					if (Strings.isNotEmpty(textValue)) {
						RefNameUtils.AuthorityTermInfo authorityTermInfo = RefNameUtils.getAuthorityTermInfo(textValue);
						final String authority = authorityTermInfo.inAuthority.resource;
						final String shortId = authorityTermInfo.inAuthority.name;

						String authorityDisplayName = authority;
						String vocabDisplayName = shortId;
						final AuthorityDisplayMapping mapping = authorityDisplayNames.get(authority);
						if (mapping != null) {
							authorityDisplayName = mapping.getAuthorityDisplayName();
							vocabDisplayName = mapping.getVocabDisplayName(shortId);
						}

						textValue = authorityDisplayName + "/" + vocabDisplayName;
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

	private String collectValues(Element element, List<String> path, int depth,
	                             boolean isRefName) {
		String delimitedValues = "";
		String fieldName = path.get(depth);
		String delimiter = (depth / 2 > 0) ? this.nestedValueDelimiter : this.valueDelimiter;
		List<Node> matches = element.selectNodes(fieldName);

		if (matches.size() > 0) {
			List<String> values = new ArrayList<>();
			boolean hasValue = false;

			for (Node node : matches) {
				String textValue;

				if (depth < path.size() - 1) {
					textValue = collectValues((Element) node, path, depth + 1, isRefName);
				} else {
					textValue = node.getText();

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

	private boolean isRefField(PoxPayloadOut document, Field field) {
		final String fieldSpec = field.getValue();
		final String[] segments = fieldSpec.split(":", 2);
		final String partName = segments[0];
		final List<String> xpath = Arrays.asList(segments[1].split("/"));
		final String fieldName = xpath.get(xpath.size() - 1);

		return isRefField(document.getName(), partName, fieldName.replaceFirst("\\[.*]", ""));
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
			String[] segments = termRefField.split("[/|]");
			String fieldName = segments[segments.length - 1];

			refFields.add(fieldName);
		}

		for (String authRefField : ServiceBindingUtils.getPropertyValuesForPart(serviceBinding, partName, ServiceBindingUtils.AUTH_REF_PROP, false)) {
			String[] segments = authRefField.split("[/|]");
			String fieldName = segments[segments.length - 1];

			refFields.add(fieldName);
		}

		if (!refFieldsByDocType.containsKey(docType)) {
			refFieldsByDocType.put(docType, new HashMap<>());
		}

		refFieldsByDocType.get(docType).put(partName, refFields);

		return refFields;
	}

	/**
	 * Create the mapping of Authority name to Authority displayName and Authority Vocabularies shortId to Authority
	 * Vocabulary displayName (title)
	 */
	private void collectAuthorityVocabs() {
		TenantBindingType tenantBinding = tenantBindingConfigReader.getTenantBinding(serviceContext.getTenantId());

		Predicate<ServiceBindingType> hasAuthorityInstances = (binding) ->
			binding.getAuthorityInstanceList() != null &&
			binding.getAuthorityInstanceList().getAuthorityInstance() != null;

		Predicate<ServiceBindingType> hasAuthorityDisplayName = (binding) ->
			binding.getDisplayName() != null && !binding.getDisplayName().isEmpty();

		tenantBinding.getServiceBindings().stream()
			.filter(hasAuthorityInstances.and(hasAuthorityDisplayName))
			.map(AuthorityDisplayMapping::new)
			.forEach((mapping) -> authorityDisplayNames.put(mapping.authority, mapping));
	}


	private static class AuthorityDisplayMapping {
		final String authority;
		final String displayName;
		final Map<String, String> vocabDisplayNames;

		public AuthorityDisplayMapping(ServiceBindingType binding) {
			this.authority = binding.getName();
			this.displayName = binding.getDisplayName();
			this.vocabDisplayNames = new HashMap<>();
			binding.getAuthorityInstanceList().getAuthorityInstance().forEach(this::addVocabMapping);
		}

		private void addVocabMapping(AuthorityInstanceType instance) {
			vocabDisplayNames.put(instance.getTitleRef(), instance.getTitle());
		}

		public String getAuthorityDisplayName() {
			return displayName;
		}

		public String getVocabDisplayName(String shortId) {
			return vocabDisplayNames.getOrDefault(shortId, shortId);
		}
	}

	private static class CollectedValue {
		private final String textValue;
		private String authorityReference;

		private CollectedValue(final String textValue) {
			this.textValue = textValue;
		}

		private CollectedValue(final String textValue, final String authorityReference) {
			this.textValue = textValue;
			this.authorityReference = authorityReference;
		}

		public String textValue() {
			return textValue;
		}

		public void setAuthorityReference(String authorityReference) {
			this.authorityReference = authorityReference;
		}

		public String authorityReference() {
			return authorityReference;
		}

		public boolean isAuthority() {
			return authorityReference != null;
		}
	}

	private static class CollectedValues {
		final List<String> values = new ArrayList<>();
		final List<String> reftypes = new ArrayList<>();
		boolean hasRefField;

		public void addValue(final String value) {
			if (hasRefField) {
				throw new IllegalStateException("addValue must only be used on non-authority based fields");
			}
			values.add(value);
		}

		public void addAuthorityValue(final String value, final String reftype) {
			if (!hasRefField) {
				throw new IllegalStateException("addAuthorityValue must only be used on authority based fields");
			}
			values.add(value);
			reftypes.add(reftype);
		}

		public void setHasRefField(final boolean hasRefField) {
			this.hasRefField = hasRefField;
		}

		public boolean hasRefField() {
			return hasRefField;
		}

		public List<String> getValues() {
			return values;
		}

		public List<String> getReftypes() {
			return reftypes;
		}
	}
}
