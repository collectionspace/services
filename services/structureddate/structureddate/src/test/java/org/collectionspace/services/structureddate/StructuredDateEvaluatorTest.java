package org.collectionspace.services.structureddate;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.yaml.snakeyaml.Yaml;

public class StructuredDateEvaluatorTest {
	public static final String TEST_CASE_FILE = "/test-dates.yaml";
	public static final List<String> YAML_DATE_SPEC = Arrays.asList("year", "month", "day", "era", "certainty", "qualifierType", "qualifierValue", "qualifierUnit");

	final Logger logger = LoggerFactory.getLogger(StructuredDateEvaluatorTest.class);

	@BeforeClass
	public void setUp() {

	};

	@Test
	public void test() {
		Yaml yaml = new Yaml();
		Map<String, Object> testCases = (Map<String, Object>) yaml.load(getClass().getResourceAsStream(TEST_CASE_FILE));

		for (String displayDate : testCases.keySet()) {
			logger.debug("Testing input: " + displayDate);

			Map<String, Object> expectedStructuredDateFields = (Map<String, Object>) testCases.get(displayDate);

			StructuredDateInternal expectedStructuredDate = createStructuredDateFromYamlSpec(displayDate, expectedStructuredDateFields);
			StructuredDateInternal actualStructuredDate = null;

			try {
				actualStructuredDate = StructuredDateInternal.parse(displayDate);
			}
			catch(StructuredDateFormatException e) {
				logger.debug(e.getMessage());
			}

			Assert.assertEquals(actualStructuredDate, expectedStructuredDate);
			logger.debug("{} was successfully parsed.", displayDate);
		}
	}

	private StructuredDateInternal createStructuredDateFromYamlSpec(String displayDate, Map<String, Object> structuredDateFields) {
		StructuredDateInternal structuredDate = null;

		if (structuredDateFields != null) {
			if (structuredDateFields.containsKey("latestDate")) {
				Object latestDate = structuredDateFields.get("latestDate");

				if (latestDate instanceof String && latestDate.equals("current date")) {
					Date currentDate = DateUtils.getCurrentDate();
					ArrayList latestDateItems = new ArrayList<>();

					latestDateItems.add(currentDate.getYear());
					latestDateItems.add(currentDate.getMonth());
					latestDateItems.add(currentDate.getDay());
					latestDateItems.add(currentDate.getEra().toDisplayString());

					structuredDateFields.put("latestDate", latestDateItems);
				}
			}

			if (!structuredDateFields.containsKey("displayDate")) {
				structuredDateFields.put("displayDate", displayDate);
			}

			if (!structuredDateFields.containsKey("scalarValuesComputed")) {
				structuredDateFields.put("scalarValuesComputed", true);
			}

			structuredDate = new StructuredDateInternal();

			for (String propertyName : structuredDateFields.keySet()) {
				Object value = structuredDateFields.get(propertyName);

				try {
					Class propertyType = PropertyUtils.getPropertyType(structuredDate, propertyName);

					if (propertyType.equals(Date.class)) {
						value = createDateFromYamlSpec((List<Object>) value);
					}

					PropertyUtils.setProperty(structuredDate, propertyName, value);
				}
				catch(NoSuchMethodException e) {
					logger.warn(propertyName + " is not a property");
				}
				catch(InvocationTargetException e) {
					logger.error(propertyName + " accessor threw an exception");
				}
				catch(IllegalAccessException e) {
					logger.error("could not access property " + propertyName);
				}
			}
		}

		return structuredDate;
	}

	private Date createDateFromYamlSpec(List<Object> dateFields) {
		Date date = new Date();
		Iterator<Object> fieldIterator = dateFields.iterator();

		for (String propertyName : YAML_DATE_SPEC) {
			Object value = fieldIterator.hasNext() ? fieldIterator.next() : null;

			try {
				Class propertyType = PropertyUtils.getPropertyType(date, propertyName);

				if (value != null && Enum.class.isAssignableFrom(propertyType)) {
					value = Enum.valueOf(propertyType, (String) value);
				}

				PropertyUtils.setProperty(date, propertyName, value);
			}
			catch(NoSuchMethodException e) {
				logger.warn(propertyName + " is not a property");
			}
			catch(InvocationTargetException e) {
				logger.error(propertyName + " accessor threw an exception");
			}
			catch(IllegalAccessException e) {
				logger.error("could not access property " + propertyName);
			}
		}

		return date;
	}
}
