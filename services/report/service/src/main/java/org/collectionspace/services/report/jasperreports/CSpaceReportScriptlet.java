package org.collectionspace.services.report.jasperreports;

import java.sql.Array;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.jasperreports.engine.JRDefaultScriptlet;
import net.sf.jasperreports.engine.JRScriptletException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRFillField;

import org.collectionspace.services.common.api.RefNameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JasperReports scriptlet to apply by default to all CollectionSpace reports. This handles the
 * formatting of refname values as display names (aka de-urning) in specified fields. The names of
 * fields to de-urn are supplied in the deurnfields parameter, as a comma-delimited list. If "*" is
 * specified, all string-typed fields are de-urned.
 */
public class CSpaceReportScriptlet extends JRDefaultScriptlet {
	private final Logger logger = LoggerFactory.getLogger(CSpaceReportScriptlet.class);

	private static String DEURN_FIELDS_PARAM = "deurnfields";

	protected boolean isDeurnAll = false;
	protected Set<String> deurnFieldNames = null;

	@Override
	public void afterReportInit() throws JRScriptletException {
		String deurnFieldsSpec = (String) this.getParameterValue(DEURN_FIELDS_PARAM, false);

		if (deurnFieldsSpec != null) {
			this.deurnFieldNames = new HashSet<String>();

			for (String fieldSpec : deurnFieldsSpec.split(",")) {
				String trimmedFieldSpec = fieldSpec.trim();

				if (trimmedFieldSpec.equals("*")) {
					this.isDeurnAll = true;
				} else {
					this.deurnFieldNames.add(trimmedFieldSpec);
				}
			}
		}
	}

	@Override
	public void beforeDetailEval() throws JRScriptletException {
		if (this.isDeurnAll) {
			deurnAllFields();
		} else {
			deurnSpecifiedFields();
		}
	}

	private void deurnAllFields() {
		if (this.fieldsMap != null) {
			for (JRFillField field : this.fieldsMap.values()) {
				if (field.getValueClass().equals(String.class)) {
					deurnField(field);
				}
			}
		}
	}

	private void deurnSpecifiedFields() {
		if (this.fieldsMap != null && this.deurnFieldNames != null) {
			for (String fieldName : this.deurnFieldNames) {
				JRFillField field = this.fieldsMap.get(fieldName);

				if (field == null) {
					logger.warn("{}: deurn field not found: {}", getReportName(), fieldName);

					continue;
				}

				if (field.getValueClass().equals(String.class)) {
					deurnField(field);
				} else if (field.getValueClass().equals(Array.class)) {
					deurnArray(field);
				} else {
					logger.warn("{}: deurn field is not a string or array: {}", getReportName(), fieldName);
				}

			}
		}
	}

	private void deurnArray(JRFillField field) {
		Array array = (Array) field.getValue();
		List<String> deurned = new ArrayList<String>();
		try {
			if (array == null) {
				return;
			}

			if (!array.getBaseTypeName().equals("varchar")) {
				logger.warn("{}: array base type is not varchar: {}", getReportName(), field.getName());
				return;
			}

			for (String value : (String[]) array.getArray()) {
				try {
					if (value != null) {
						deurned.add(RefNameUtils.getDisplayName(value));
					}
				} catch (IllegalArgumentException ex) {
					logger.debug("{}: skipping {}", getReportName(), value);
					deurned.add(value);
				}
			}
		} catch (SQLException e) {
			logger.warn("{}: array could not be read for field: {}", getReportName(), field.getName(), e);
		}

		field.setValue(new DeurnArray(deurned));
	}

	private void deurnField(JRFillField field) {
		String value = (String) field.getValue();

		if (value != null) {
			try {
				field.setValue(RefNameUtils.getDisplayName(value));
			} catch (IllegalArgumentException ex) {
				// It wasn't a valid refname. Keep the value.
			}
		}
	}

	private String getReportName() {
		JasperReport report = null;

		try {
			report = (JasperReport) this.getParameterValue("JASPER_REPORT", false);
		}
		catch (JRScriptletException ex) {}

		return (report != null ? report.getName() : "Unknown report name");
	}
}
