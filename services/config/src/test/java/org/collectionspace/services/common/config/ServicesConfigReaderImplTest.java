package org.collectionspace.services.common.config;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.collectionspace.services.config.AssertionAttributeProbeType;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.SAMLType;
import org.collectionspace.services.config.ServiceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

public class ServicesConfigReaderImplTest {
	private static final Logger logger = LoggerFactory.getLogger(ServicesConfigReaderImplTest.class);
	private static String BANNER = "-------------------------------------------------------";
	// NOTE: adapted from https://collectionspace.atlassian.net/browse/DRYD-1702?focusedCommentId=60649
	private static final String USERNAME_ATTRIBUTE = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress";
	private static final String SSOID_ATTRIBUTE = "http://schemas.auth0.com/identifier";
	private static final String SSO_CONFIG_STRING = new StringBuilder()
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
			.append("<attribute name=\"" + USERNAME_ATTRIBUTE + "\" />")
			.append("</assertion-username-probes>")
			.append("<assertion-sso-id-probes>")
			.append("<attribute name=\"" + SSOID_ATTRIBUTE + "\" />")
			.append("</assertion-sso-id-probes>")
			.append("</relying-party>")
			.append("</relying-party-registrations>")
			.append("</saml>")
			.append("</sso>\n")
			.append("</security>")
			.append("</svc:service-config>")
			.toString();
	private static final String MOCK_ROOT_DIR = "./";
	private void testBanner(String msg) {      
        logger.info("\r" + BANNER + "\r\n" + this.getClass().getName() + "\r\n" + msg + "\r\n" + BANNER);
    }
	
	// the tests are below
	private ServiceConfig serviceConfig = null;
	@BeforeSuite
	public void setup() throws JAXBException {
		ServicesConfigReaderImpl rdr = new ServicesConfigReaderImpl(MOCK_ROOT_DIR);
		ByteArrayInputStream in = new ByteArrayInputStream(SSO_CONFIG_STRING.getBytes());
		try {
			serviceConfig = (ServiceConfig) rdr.parse(in, ServiceConfig.class);
		} catch (JAXBException e) {
			logger.warn("Could not create test service config: " + e.getLocalizedMessage());
			throw e;
		}
	}
	private List<AssertionAttributeProbeType> getUserNameProbesFromConfig() {
		return getUserNameProbesFromConfig(serviceConfig);
	}
	private List<AssertionAttributeProbeType> getUserNameProbesFromConfig(ServiceConfig serviceConfig) {
		SAMLType samlConfig = serviceConfig.getSecurity().getSso().getSaml();
		List<SAMLRelyingPartyType> relyingParties = samlConfig.getRelyingPartyRegistrations().getRelyingParty();
		SAMLRelyingPartyType relyingParty = relyingParties.get(0);
		
		List<Object> usernameProbes = relyingParty.getAssertionUsernameProbes().getNameIdOrAttribute();
		ArrayList<AssertionAttributeProbeType> up = new ArrayList<AssertionAttributeProbeType>();
		for (Object obj : usernameProbes) {
			AssertionAttributeProbeType a = (AssertionAttributeProbeType) obj;
			up.add(a);
		}
		
		return up;
	}
	private List<AssertionAttributeProbeType> getSsoIdProbesFromConfig() {
		return getSsoIdProbesFromConfig(serviceConfig);
	}
	private List<AssertionAttributeProbeType> getSsoIdProbesFromConfig(ServiceConfig serviceConfig) {
		SAMLType samlConfig = serviceConfig.getSecurity().getSso().getSaml();
		List<SAMLRelyingPartyType> relyingParties = samlConfig.getRelyingPartyRegistrations().getRelyingParty();
		SAMLRelyingPartyType relyingParty = relyingParties.get(0);
		
		List<Object> ssoIdProbes = relyingParty.getAssertionSsoIdProbes().getNameIdOrAttribute();
		ArrayList<AssertionAttributeProbeType> up = new ArrayList<AssertionAttributeProbeType>();
		for (Object obj : ssoIdProbes) {
			AssertionAttributeProbeType a = (AssertionAttributeProbeType) obj;
			up.add(a);
		}
		
		return up;
	}
	@Test
	public void usernameProbesNotNullOrEmpty() {
		testBanner("the username probes list is not null or empty");
		
		List<AssertionAttributeProbeType> usernameProbes = getUserNameProbesFromConfig();
		Assert.assertNotNull(usernameProbes);
		if(null != usernameProbes) {
			Assert.assertFalse(usernameProbes.isEmpty());
		}
	}
	@Test(dependsOnMethods = {"usernameProbesNotNullOrEmpty"})
	public void usernameProbesCorrectlyParsedFromConfig() {
		testBanner("the username probes list has expected contents");
		
		List<AssertionAttributeProbeType> usernameProbes = getUserNameProbesFromConfig();
		Assert.assertEquals(usernameProbes.size(), 1);
		AssertionAttributeProbeType probe = usernameProbes.get(0);
		Assert.assertEquals(probe.getName(), USERNAME_ATTRIBUTE);
	}
	@Test
	public void ssoIdProbesNotNullOrEmpty() {
		testBanner("the SSO ID probes list is not null or empty");
		
		List<AssertionAttributeProbeType> ssoIdProbes = getSsoIdProbesFromConfig();
		Assert.assertNotNull(ssoIdProbes);
		if(null != ssoIdProbes) {
			Assert.assertFalse(ssoIdProbes.isEmpty());
		}
	}
	@Test(dependsOnMethods = {"ssoIdProbesNotNullOrEmpty"})
	public void ssoIdProbesCorrectlyParsedFromConfig() {
		testBanner("the SSO ID probes list has expected contents");
		
		List<AssertionAttributeProbeType> ssoIdProbes = getSsoIdProbesFromConfig();
		Assert.assertEquals(ssoIdProbes.size(), 1);
		AssertionAttributeProbeType probe = ssoIdProbes.get(0);
		Assert.assertEquals(probe.getName(), SSOID_ATTRIBUTE);
	}
}
