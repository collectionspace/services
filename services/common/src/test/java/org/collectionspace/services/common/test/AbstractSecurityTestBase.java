package org.collectionspace.services.common.test;

import java.io.ByteArrayInputStream;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import org.collectionspace.services.common.config.ServicesConfigReaderImpl;
import org.collectionspace.services.config.ServiceConfig;
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
	protected static final String USERNAME_ATTRIBUTE = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
	protected static final String SSOID_ATTRIBUTE = "http://schemas.auth0.com/identifier";
	protected static final String SSO_CONFIG_STRING = createDefaultTestConfig();
	protected static String createDefaultTestConfig() {
		return createTestConfig(USERNAME_ATTRIBUTE, SSOID_ATTRIBUTE);
	}
	protected static String createTestConfig(String usernameAttribute, String ssoAttribute) {
		return new StringBuilder()
				.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
				.append("<svc:service-config xmlns:svc='http://collectionspace.org/services/config'>")
				.append("<security>")
				.append("<sso>")
				.append("<saml>")
				.append("<single-logout />")
				.append("<relying-party-registrations>")
				.append("<relying-party id=\"auth0\">")
				.append("<name>Auth0 - Scenario 11</name>")
				.append("<icon location=\"https://cdn.auth0.com/manhattan/versions/1.4478.0/assets/badge.png\" />")
				.append("<metadata location=\"https://dev-cf0ltyyfory6gtqm.us.auth0.com/samlp/metadata/ZXtZfEN0mj96GP8LCmEUWcpuDO0OtqKY\" />")
				.append("<assertion-username-probes>")
				.append("<attribute name=\"" + usernameAttribute + "\" />")
				.append("</assertion-username-probes>")
				.append("<assertion-sso-id-probes>")
				.append("<attribute name=\"" + ssoAttribute + "\" />")
				.append("</assertion-sso-id-probes>")
				.append("</relying-party>")
				.append("</relying-party-registrations>")
				.append("</saml>")
				.append("</sso>\n")
				.append("</security>")
				.append("</svc:service-config>")
				.toString();
	}
	protected static final String MOCK_ROOT_DIR = "./";
    protected ServiceConfig parseServiceConfigString() throws JAXBException {
    	return parseServiceConfigString(MOCK_ROOT_DIR, SSO_CONFIG_STRING);
    }
	protected ServiceConfig parseServiceConfigString(String mockRootDir, String seviceConfigString) throws JAXBException {
		ServicesConfigReaderImpl rdr = new ServicesConfigReaderImpl(mockRootDir);
		ByteArrayInputStream in = new ByteArrayInputStream(seviceConfigString.getBytes());
		try {
			serviceConfig = (ServiceConfig) rdr.parse(in, ServiceConfig.class);
		} catch (JAXBException e) {
			logger.warn("Could not create test service config: " + e.getLocalizedMessage());
			throw e;
		}
		return serviceConfig;
	}
	
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
	protected Attribute createTestAttribute(
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
	protected Attribute createDefaultTestAttribute(boolean hasTypedAttributeValues) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createTestAttribute(hasTypedAttributeValues, ATTR_NAME, ATTR_NAME_FORMAT);
    }
	protected Assertion createTestAssertion(Attribute attribute) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Assertion testAssertion = createTestAssertionNoAttributes();
		
		AttributeStatement attrStmt = createNewSAMLObject(AttributeStatement.class);
		attrStmt.getAttributes().add(attribute);
		testAssertion.getAttributeStatements().add(attrStmt);
    	
		return testAssertion;
	}
	protected Assertion createTestAssertionTypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createTestAssertion(createDefaultTestAttribute(true));
    }
	protected Assertion createTestAssertionUntypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		return createTestAssertion(createDefaultTestAttribute(false));
    }
	
	/* test suite setup below */
    protected Assertion testAssertionTypedAttributeValues = null;
    protected Assertion testAssertionUntypedAttributeValues = null;
    protected ServiceConfig serviceConfig = null;
    @BeforeSuite
    protected void setup() throws InitializationException,NoSuchFieldException,IllegalAccessException, JAXBException {
    	/* try to set up openSAML */
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
		
		/* try to set up mock config */
		serviceConfig = parseServiceConfigString();
    }
}
