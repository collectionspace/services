package org.collectionspace.services.common.test;

import javax.xml.namespace.QName;

import org.joda.time.DateTime;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;

public class AbstractSecurityTestBase {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtilsTest.class);
	protected static String BANNER = "-------------------------------------------------------";
	protected static String FRIENDLY_ATTR_NAME = "mail";
	protected static String ATTR_NAME = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
	protected static String ATTR_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
	protected static String EMAIL_ADDRESS = "example@example.org";

	/* for mocking useful SAML objects */
	protected <T extends SAMLObject> T createNewSAMLObject(Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
    	
    	@SuppressWarnings("unchecked") // NOTE: the T extends SAMLObject ought to guarantee this works
		T theObject = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
    	return theObject;
    }
	protected XSString createNewXSString(String value) {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSString> stringBuilder = (XMLObjectBuilder<XSString>) builderFactory.getBuilder(XSString.TYPE_NAME);
    	XSString theString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
    	theString.setValue(value);
    	return theString;
    }
    // NOTE: making the assumption that OpenSAML parses an untyped attribute value into XSAny with value in the text content 
	protected XSAny createNewXSAny(String value) {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSAny> stringBuilder = (XMLObjectBuilder<XSAny>) builderFactory.getBuilder(XSAny.TYPE_NAME);
    	XSAny theAny = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,XSAny.TYPE_NAME);
    	theAny.setTextContent(value);
    	return theAny;
    }
	protected Assertion createTestAssertionNoAttributes() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    	Assertion testAssertion = createNewSAMLObject(Assertion.class);
		testAssertion.setVersion(SAMLVersion.VERSION_20);
		testAssertion.setIssueInstant(new DateTime());

		Subject testSubject = createNewSAMLObject(Subject.class);
		NameID testNameId = createNewSAMLObject(NameID.class);
		testNameId.setValue("test subject nameid");
		testSubject.setNameID(testNameId);
		testAssertion.setSubject(testSubject);
		
    	return testAssertion;
    }
	protected Attribute createAttribute(
			boolean hasTypedAttributeValues,
			String attributeName,
			String attributeNameFormat
		) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Attribute attr = createNewSAMLObject(Attribute.class);
		attr.setFriendlyName(FRIENDLY_ATTR_NAME);
		attr.setName(attributeName);
		attr.setNameFormat(attributeNameFormat);
		if(hasTypedAttributeValues) {
			XSString attrValue = createNewXSString(EMAIL_ADDRESS);
			attr.getAttributeValues().add(attrValue);
		}
		else {
			XSAny attrValue = createNewXSAny(EMAIL_ADDRESS);
			attr.getAttributeValues().add(attrValue);
		}
		
		return attr;
	}
	protected Attribute createDefaultAttribute(boolean hasTypedAttributeValues) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createAttribute(hasTypedAttributeValues, ATTR_NAME, ATTR_NAME_FORMAT);
    }
	protected Assertion createTestAssertion(Attribute attribute) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Assertion testAssertion = createTestAssertionNoAttributes();
		
		AttributeStatement attrStmt = createNewSAMLObject(AttributeStatement.class);
		attrStmt.getAttributes().add(attribute);
		testAssertion.getAttributeStatements().add(attrStmt);
    	
		return testAssertion;
	}
	protected Assertion createTestAssertionTypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createTestAssertion(createDefaultAttribute(true));
    }
	protected Assertion createTestAssertionUntypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createTestAssertion(createDefaultAttribute(false));
    }
	
    protected Assertion testAssertionTypedAttributeValues = null;
    protected Assertion testAssertionUntypedAttributeValues = null;
    @BeforeSuite
    private void setup() throws InitializationException,NoSuchFieldException,IllegalAccessException {
    	// try to set up openSAML
		XMLObjectProviderRegistry registry = new XMLObjectProviderRegistry();
		ConfigurationService.register(XMLObjectProviderRegistry.class, registry);
		try {
			InitializationService.initialize();
		} catch (InitializationException e) {
			logger.error("Could not initialize openSAML: " + e.getLocalizedMessage(), e);
			throw e;
		}	
		// try to create a test assertion with typed attribute values; fail the test if this doesn't work
		try {
			testAssertionTypedAttributeValues = createTestAssertionTypedAttributeValues();
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.error("Could not create test assertion with typed attribute values: " + e.getLocalizedMessage(), e);
			throw e;
		}
		// try to create a test assertion with untyped attribute values; fail the test if this doesn't work
		try {
			testAssertionUntypedAttributeValues = createTestAssertionUntypedAttributeValues();
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.error("Could not create test assertion with untyped attribute values: " + e.getLocalizedMessage(), e);
			throw e;
		}
    }
}
