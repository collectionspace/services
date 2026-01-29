package org.collectionspace.services.export;

import static org.collectionspace.services.common.context.ServiceBindingUtils.AUTH_REF_PROP;
import static org.collectionspace.services.common.context.ServiceBindingUtils.TERM_REF_PROP;
import static org.collectionspace.services.common.context.ServiceBindingUtils.getPropertyValuesForPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.util.Strings;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefNameUtils;
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
	private static final String INCLUDE_AUTHORITY_PARAM = "includeauthority";
	private static final String AUTHORITY_SUFFIX = "AuthorityVocabulary";

	private CSVPrinter csvPrinter;
	private Map<String, Table<String, String, Set<String>>> refFieldsByDocType = new HashMap<>();
	private String valueDelimiter = "|";
	private String nestedValueDelimiter = "^^";
	private boolean includeAuthority = false;

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
				} else if (param.getKey().equals(INCLUDE_AUTHORITY_PARAM)) {
					this.includeAuthority = Boolean.parseBoolean(param.getValue());
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

			// Create additional authority vocabulary column if requested
			if (includeAuthority) {
				String partName = fieldSpec.split(":", 2)[0];
				String docType = partName.substring(0, partName.indexOf("_"));
				FieldType fieldType = getFieldType(docType, partName, fieldName);
				if (fieldType == FieldType.AUTH_REF) {
					headers.add(fieldName + AUTHORITY_SUFFIX);
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
			FieldType fieldType = getFieldType(document, field);

			csvRecord.add(collectValues(document, field.getValue(), fieldType, ColumnType.FIELD));
			if (fieldType == FieldType.AUTH_REF && includeAuthority) {
				csvRecord.add(collectValues(document, field.getValue(), fieldType, ColumnType.AUTHORITY));
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

	private String collectValues(PoxPayloadOut document, String fieldSpec, FieldType fieldType, ColumnType columnType) {
		String delimitedValues = "";
		String[] segments = fieldSpec.split(":", 2);
		String partName = segments[0];
		String xpath = segments[1];

		PayloadOutputPart part = document.getPart(partName);

		if (part != null) {
			delimitedValues = collectValues(part.getElementBody(), Arrays.asList(xpath.split("/")), 0, fieldType,
			                                columnType);
		}

		return delimitedValues;
	}

	private String collectValues(Element element, List<String> path, int depth, FieldType fieldType,
	                             ColumnType columnType) {
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
					textValue = collectValues((Element) node, path, depth + 1, fieldType, columnType);
				} else if (columnType == ColumnType.AUTHORITY) {
					textValue = node.getText();

					if (Strings.isNotEmpty(textValue)) {
						final RefNameUtils.AuthorityTermInfo termInfo = RefNameUtils.getAuthorityTermInfo(textValue);
						final String authority = termInfo.inAuthority.resource;
						final String shortId = termInfo.inAuthority.name;

						String authorityDisplayName = authority;
						String vocabDisplayName = shortId;
						final AuthorityDisplayMapping mapping = authorityDisplayNames.get(authority);
						if (mapping != null) {
							authorityDisplayName = mapping.getAuthorityDisplayName();
							vocabDisplayName = mapping.getVocabDisplayName(shortId);
						}

						textValue = authorityDisplayName + "/" + vocabDisplayName;
					}
				} else {
					textValue = node.getText();

					if (fieldType.isRefField() && StringUtils.isNotEmpty(textValue)) {
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

	/**
	 * Get the {@link FieldType} for a given {@link Field}. We expect {@link Field#getValue} to return
	 * something which matches our Field Spec, i.e. "namespace:path/to/field". We allow for some xpath operations,
	 * such as contains, so we also need to be mindful of that and filter them out appropriately.
	 *
	 * @param document the document we're searching in
	 * @param field the field being searched for
	 * @return the field type
	 */
	private FieldType getFieldType(PoxPayloadOut document, Field field) {
		final String fieldSpec = field.getValue();
		final String[] segments = fieldSpec.split(":", 2);
		final String partName = segments[0];
		final List<String> xpath = Arrays.asList(segments[1].split("/"));
		final String fieldName = xpath.get(xpath.size() - 1);

		return getFieldType(document.getName(), partName, fieldName.replaceFirst("\\[.*]", ""));
	}

	private FieldType getFieldType(String docType, String partName, String fieldName) {
		Map<String, Set<String>> refs = getRefFields(docType, partName);
		if (refs.getOrDefault(TERM_REF_PROP, Collections.emptySet()).contains(fieldName)) {
			return FieldType.TERM_REF;
		} else if (refs.getOrDefault(AUTH_REF_PROP, Collections.emptySet()).contains(fieldName)) {
			return FieldType.AUTH_REF;
		}

		return FieldType.STANDARD;
	}

	private Map<String, Set<String>> getRefFields(String docType, String partName) {
		boolean containsDocTypeAndPart = refFieldsByDocType.containsKey(docType)
				&& refFieldsByDocType.get(docType).containsRow(partName);
		Map<String, Set<String>> refFields = containsDocTypeAndPart ? refFieldsByDocType.get(docType).row(partName) : null;

		if (refFields != null) {
			return refFields;
		}

		Set<String> authRefs = new HashSet<>();
		Set<String> termRefs = new HashSet<>();
		Table<String, String, Set<String>> refTable = HashBasedTable.create();

		ServiceBindingType serviceBinding =
			tenantBindingConfigReader.getServiceBinding(serviceContext.getTenantId(), docType);

		for (String termRefField : getPropertyValuesForPart(serviceBinding, partName, TERM_REF_PROP, false)) {
			String[] segments = termRefField.split("[/|]");
			String fieldName = segments[segments.length - 1];

			termRefs.add(fieldName);
		}

		for (String authRefField : getPropertyValuesForPart(serviceBinding, partName, AUTH_REF_PROP, false)) {
			String[] segments = authRefField.split("[/|]");
			String fieldName = segments[segments.length - 1];

			authRefs.add(fieldName);
		}

		refTable.put(partName, TERM_REF_PROP, termRefs);
		refTable.put(partName, AUTH_REF_PROP, authRefs);

		if (!refFieldsByDocType.containsKey(docType)) {
			refFieldsByDocType.put(docType, HashBasedTable.create());
		}

		refFieldsByDocType.get(docType).putAll(refTable);
		return refTable.row(partName);
	}

	/**
	 * Create the mapping of Authority name to Authority displayName and Authority Vocabularies shortId to Authority
	 * Vocabulary displayName (title)
	 */
	private void collectAuthorityVocabs() {
		if (!includeAuthority) {
			return;
		}

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

	/**
	 * The type of column which we want to collect data for.
	 * Field - the text value or display name if a refname is present
	 * Authority - The aggregate of the Authority display name and Authority Vocabulary display name
	 */
	private enum ColumnType {
		FIELD, AUTHORITY
	}

	private enum FieldType {
		STANDARD, TERM_REF, AUTH_REF;

		public boolean isRefField() {
			return this == TERM_REF || this == AUTH_REF;
		}
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
}
