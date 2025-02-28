package org.collectionspace.services.common.test;

import java.util.Set;

import javax.xml.bind.JAXBException;

import org.collectionspace.services.common.security.SecurityUtils;
import org.collectionspace.services.config.AssertionProbesType;
import org.collectionspace.services.config.SAMLRelyingPartyRegistrationsType;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.ServiceConfig;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
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
	@Test
	public void idenfitiferProbeFindsSsoId() throws JAXBException,IllegalArgumentException,IllegalAccessException,NoSuchFieldException,SecurityException
	{
		testBanner("identifier probe finds sso id");
		
		String nameFormat = "urn:oasis:names:tc:SAML:2.0:attrname-format:uri";
		String identifierName = "http://schemas.auth0.com/identifier";
		
		// set up a minimal mock configuration string with the SSO ID probe we wish to test
		String theConfigString = createTestConfig(USERNAME_ATTRIBUTE, identifierName);
		ServiceConfig theServiceConfig = null;
		try {
			theServiceConfig = parseServiceConfigString(MOCK_ROOT_DIR, theConfigString);
		} catch (JAXBException e) {
			logger.warn("Could not create mock service config: " + e.getLocalizedMessage());
			throw e;
		}
		SAMLRelyingPartyRegistrationsType relyingPartyRegistrations = theServiceConfig.getSecurity().getSso().getSaml().getRelyingPartyRegistrations();
		SAMLRelyingPartyType relyingPartyRegistration = relyingPartyRegistrations.getRelyingParty().get(0);
		AssertionProbesType assertionSsoIdProbes = (relyingPartyRegistration != null
				? relyingPartyRegistration.getAssertionSsoIdProbes()
				: null);
		
		// create an attribute with the same name identifier as the test probe
		Attribute attribute = null;
		try {
			attribute = createTestAttribute(true, identifierName, nameFormat);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.warn("Could not create mock attribute: " + e.getLocalizedMessage());
			throw e;
		}
		// create a SAML assertion with the attribute
		Assertion assertion = null;
		try {
			assertion = createTestAssertion(attribute);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			logger.warn("Could not create SAML assertion" + e.getLocalizedMessage());
			throw e;
		}
		
		// check whether getSamlAssertionSsoId finds the SSO ID we put in the assertion using the test probe
		String ssoId = SecurityUtils.getSamlAssertionSsoId(assertion, assertionSsoIdProbes);
		Assert.assertNotNull(ssoId);
		Assert.assertFalse(ssoId.isEmpty());
		Assert.assertEquals(ssoId,EMAIL_ADDRESS);
	}
}
