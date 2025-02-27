package org.collectionspace.services.common.test;

import java.util.Set;

import javax.xml.namespace.QName;

import org.collectionspace.services.common.security.SecurityUtils;
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
import org.testng.Assert;
import org.testng.annotations.Test;

public class SecurityUtilsTest extends AbstractSecurityTestBase {
	private static final Logger logger = LoggerFactory.getLogger(SecurityUtilsTest.class);
	private void testBanner(String msg) {      
        logger.info("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
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
    @Test(dependsOnMethods = {"assertionWithUntypedAttributeValuesIsNotNull"})
    public void candidateUsernamesUntypedIsCorrect() {
    	testBanner("findSamlAssertionCandidateUsernames finds candidate usernames when they are not typed");
    	Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(testAssertionUntypedAttributeValues, null);
		Assert.assertNotNull(candidateUsernames);
		if(null != candidateUsernames)
			Assert.assertEquals(candidateUsernames.iterator().next(),EMAIL_ADDRESS);
    }
    @Test(dependsOnMethods = {"assertionWithTypedAttributeValuesIsNotNull"})
    public void candidateUsernamesTypedIsCorrect() {
    	testBanner("findSamlAssertionCandidateUsernames finds candidate usernames when they are typed as string");
    	Set<String> candidateUsernames = SecurityUtils.findSamlAssertionCandidateUsernames(testAssertionTypedAttributeValues, null);
		Assert.assertNotNull(candidateUsernames);
		if(null != candidateUsernames)
			Assert.assertEquals(candidateUsernames.iterator().next(),EMAIL_ADDRESS);
    }
}
