package org.collectionspace.services.common.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.config.AssertionAttributeProbeType;
import org.collectionspace.services.config.AssertionProbesType;
import org.joda.time.DateTime;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.config.XMLObjectProviderRegistrySupport;
import org.opensaml.core.xml.io.Marshaller;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.schema.XSAny;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.saml.common.SAMLObject;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;

public class SecurityUtilsTest {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtilsTest.class);
	private static String BANNER = "-------------------------------------------------------";
	private static String FRIENDLY_ATTR_NAME = "mail";
	private static String ATTR_NAME = "urn:oid:0.9.2342.19200300.100.1.3";
	private static String ATTR_NAME_FORMAT = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
	private static String EMAIL_ADDRESS = "example@example.org";
	private void testBanner(String msg) {      
        logger.info("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }
	/*
	private String xml2String(XMLObject xmlObject) {
		Element element = null;
		String xmlString = "<uninitialized>";
        try {
            Marshaller out = XMLObjectProviderRegistrySupport.getMarshallerFactory().getMarshaller(xmlObject);
            out.marshall(xmlObject);
            element = xmlObject.getDOM();

        } catch (MarshallingException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }

        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(new java.io.StringWriter());
            
            transformer.transform(new DOMSource(element), result);
            xmlString = result.getWriter().toString();
        } catch (TransformerConfigurationException e) {
        	logger.error("Transformer configuration exception: " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (TransformerException e) {
        	logger.error("Exception in transformer: " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        
        return xmlString;
	}
	*/
	private <T extends SAMLObject> T createNewSAMLObject(Class<T> clazz) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	QName defaultElementName = (QName) clazz.getDeclaredField("DEFAULT_ELEMENT_NAME").get(null);
    	
    	@SuppressWarnings("unchecked") // NOTE: the T extends SAMLObject ought to guarantee this works
		T theObject = (T) builderFactory.getBuilder(defaultElementName).buildObject(defaultElementName);
    	return theObject;
    }
    private XSString createNewXSString(String value) {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSString> stringBuilder = (XMLObjectBuilder<XSString>) builderFactory.getBuilder(XSString.TYPE_NAME);
    	XSString theString = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
    	theString.setValue(value);
    	return theString;
    }
    // NOTE: making the assumption that OpenSAML parses an untyped attribute value into XSAny with value in the text content 
    private XSAny createNewXSAny(String value) {
    	XMLObjectBuilderFactory builderFactory = XMLObjectProviderRegistrySupport.getBuilderFactory();
    	@SuppressWarnings("unchecked")
		XMLObjectBuilder<XSAny> stringBuilder = (XMLObjectBuilder<XSAny>) builderFactory.getBuilder(XSAny.TYPE_NAME);
    	XSAny theAny = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME,XSAny.TYPE_NAME);
    	theAny.setTextContent(value);
    	return theAny;
    }
    private Assertion createTestAssertionNoAttributes() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
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
    private Attribute createAttribute(boolean hasTypedAttributeValues) throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Attribute attr = createNewSAMLObject(Attribute.class);
		attr.setFriendlyName(FRIENDLY_ATTR_NAME);
		attr.setName(ATTR_NAME);
		attr.setNameFormat(ATTR_NAME_FORMAT);
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
    private Assertion createTestAssertionTypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Assertion testAssertion = createTestAssertionNoAttributes();

		Attribute attr = createAttribute(true);
		
		AttributeStatement attrStmt = createNewSAMLObject(AttributeStatement.class);
		attrStmt.getAttributes().add(attr);
		testAssertion.getAttributeStatements().add(attrStmt);
    	
		return testAssertion;
    }
    private Assertion createTestAssertionUntypedAttributeValues() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		Assertion testAssertion = createTestAssertionNoAttributes();

		Attribute attr = createAttribute(false);
		
		AttributeStatement attrStmt = createNewSAMLObject(AttributeStatement.class);
		attrStmt.getAttributes().add(attr);
		testAssertion.getAttributeStatements().add(attrStmt);
    	
		return testAssertion;
    }

    // the tests are below
    private Assertion testAssertionTypedAttributeValues = null;
    private Assertion testAssertionUntypedAttributeValues = null;
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
    
    @Test
    public void assertionWithTypedAttributeValuesIsNotNull() {
    	testBanner("the mock assertion with typed attribute values is not null");
    	Assert.assertNotNull(testAssertionTypedAttributeValues);
    }
    @Test
    public void assertionWithUntypedAttributeValuesIsNotNull() {
    	testBanner("the mock assertion with untyped attribute values is not null");
    	Assert.assertNotNull(testAssertionUntypedAttributeValues);
    }
    @Test(dependsOnMethods = {"assertionWithTypedAttributeValuesIsNotNull"})
    public void candidateUsernamesTypedNotNullOrEmpty() {
    	testBanner("findSamlAssertionCandidateUsernames finds candidate usernames when they are typed as string");
    	Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(testAssertionTypedAttributeValues, null);
		Assert.assertNotNull(candidateUsernames);
		if(null != candidateUsernames)
			Assert.assertFalse(candidateUsernames.isEmpty());
    }
    @Test(dependsOnMethods = {"assertionWithUntypedAttributeValuesIsNotNull"})
    public void candidateUsernamesUntypedNotNullOrEmpty() {
    	testBanner("findSamlAssertionCandidateUsernames finds candidate usernames when they are not typed");
    	Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(testAssertionUntypedAttributeValues, null);
		Assert.assertNotNull(candidateUsernames);
		if(null != candidateUsernames)
			Assert.assertFalse(candidateUsernames.isEmpty());
    }
}
