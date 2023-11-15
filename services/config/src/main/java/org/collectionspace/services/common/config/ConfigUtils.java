package org.collectionspace.services.common.config;

import java.net.MalformedURLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.config.CORSType;
import org.collectionspace.services.config.OAuthClientRegistrationsType;
import org.collectionspace.services.config.OAuthClientType;
import org.collectionspace.services.config.OAuthType;
import org.collectionspace.services.config.SAMLRelyingPartyRegistrationsType;
import org.collectionspace.services.config.SAMLRelyingPartyType;
import org.collectionspace.services.config.SAMLType;
import org.collectionspace.services.config.SSOType;
import org.collectionspace.services.config.SecurityType;
import org.collectionspace.services.config.ServiceConfig;
import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.collectionspace.services.config.tenant.UIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {
    final static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);

    public static final String EXTENSION_XPATH = "/extension[@point='%s']";
    public static final String COMPONENT_EXTENSION_XPATH = "/component" + EXTENSION_XPATH;
    public static final String DATASOURCE_EXTENSION_POINT_XPATH = String.format(COMPONENT_EXTENSION_XPATH, "datasources");
    public static final String REPOSITORY_EXTENSION_POINT_XPATH = String.format(COMPONENT_EXTENSION_XPATH, "repository");
    public static final String CS_TENANT_DATASOURCE_VALUE = "jdbc/TenantDS";
    public static final String CONFIGURATION_EXTENSION_POINT_XPATH = String.format(COMPONENT_EXTENSION_XPATH, "configuration");
    public static final String ELASTICSEARCH_INDEX_EXTENSION_XPATH = String.format(EXTENSION_XPATH, "elasticSearchIndex");
    public static final String ELASTICSEARCH_EXTENSIONS_EXPANDER_STR = "%elasticSearchIndex_extensions%";


    // Default database names

    // public static String DEFAULT_CSPACE_DATABASE_NAME = "cspace";
    public static String DEFAULT_NUXEO_REPOSITORY_NAME = "default";
    public static String DEFAULT_NUXEO_DATABASE_NAME = "nuxeo";
    public static String DEFAULT_ELASTICSEARCH_INDEX_NAME = "nuxeo";

    /*
     * Returns the list of repository/DB names defined by a tenant bindings file
     */
    public static List<String> getRepositoryNameList(TenantBindingType tenantBindingType) {
    	List<String> result = null;

		List<RepositoryDomainType> repoDomainList = tenantBindingType.getRepositoryDomain();
		if (repoDomainList != null && repoDomainList.isEmpty() == false) {
			result = new ArrayList<String>();
			for (RepositoryDomainType repoDomain : repoDomainList) {
					result.add(repoDomain.getRepositoryName());
			}
		}

    	return result;
    }

    /*
     * Returns 'true' if the tenant declares the default repository.
     */
    public static boolean containsDefaultRepository(List<RepositoryDomainType> repoDomainList) {
    	boolean result = false;

		if (repoDomainList != null && repoDomainList.isEmpty() == false) {
			for (RepositoryDomainType repoDomain : repoDomainList) {
				if (repoDomain.isDefaultRepository() == true) {
					result = true;
					break;
				}
			}
		}

    	return result;
    }

    public static String getRepositoryName(TenantBindingType tenantBindingType, String domainName) {
		String result = null;

		if (domainName != null && domainName.trim().isEmpty() == false) {
			List<RepositoryDomainType> repoDomainList = tenantBindingType.getRepositoryDomain();
			if (repoDomainList != null && repoDomainList.isEmpty() == false) {
				for (RepositoryDomainType repoDomain : repoDomainList) {
					if (repoDomain.getName().equalsIgnoreCase(domainName)) {
						result = repoDomain.getRepositoryName();
						break; // We found a match so exit the loop
					}
				}
			}
		} else {
			logger.error(String.format("There was no domain name specified on a call to getRepositoryName() method."));
		}

		if (result == null && logger.isTraceEnabled()) {
			logger.trace(String.format("Could not find the repository name for tenent name='%s' and domain='%s'",
					tenantBindingType.getName(), domainName));
		}

		return result;
	}

	public static CORSType getCors(ServiceConfig serviceConfig) {
		SecurityType security = serviceConfig.getSecurity();

		if (security != null) {
			CORSType cors = security.getCors();

			return cors;
		}

		return null;
	}

	public static List<String> getCorsAllowedOrigins(ServiceConfig serviceConfig) {
		CORSType cors = getCors(serviceConfig);

		if (cors != null) {
			List<String> allowedOrigin = cors.getAllowedOrigin();

			if (allowedOrigin != null) {
				return allowedOrigin;
			}
		}

		return new ArrayList<String>();
	}

	public static Duration getCorsMaxAge(ServiceConfig serviceConfig) {
		CORSType cors = getCors(serviceConfig);

		if (cors != null) {
			String maxAge = cors.getMaxAge();

			if (maxAge != null) {
				return Duration.parse(maxAge);
			}
		}

		return null;
	}

	public static OAuthType getOAuth(ServiceConfig serviceConfig) {
		SecurityType security = serviceConfig.getSecurity();

		if (security != null) {
			OAuthType oauth = security.getOauth();

			return oauth;
		}

		return null;
	}

	public static List<OAuthClientType> getOAuthClientRegistrations(ServiceConfig serviceConfig) {
		OAuthType oauth = getOAuth(serviceConfig);

		if (oauth != null) {
			OAuthClientRegistrationsType registrations = oauth.getClientRegistrations();

			if (registrations != null) {
				return registrations.getClient();
			}
		}

		return null;
	}

	public static SSOType getSSO(ServiceConfig serviceConfig) {
		SecurityType security = serviceConfig.getSecurity();

		if (security != null) {
			return security.getSso();
		}

		return null;
	}

	public static SAMLType getSAML(ServiceConfig serviceConfig) {
		SSOType sso = getSSO(serviceConfig);

		if (sso != null) {
			return sso.getSaml();
		}

		return null;
	}

	public static boolean isSAMLSingleLogoutEnabled(ServiceConfig serviceConfig) {
		SAMLType saml = getSAML(serviceConfig);

		if (saml != null) {
			return (saml.getSingleLogout() != null);
		}

		return false;
	}

	public static List<SAMLRelyingPartyType> getSAMLRelyingPartyRegistrations(ServiceConfig serviceConfig) {
		SAMLType saml = getSAML(serviceConfig);

		if (saml != null) {
			SAMLRelyingPartyRegistrationsType registrations = saml.getRelyingPartyRegistrations();

			if (registrations != null) {
				return registrations.getRelyingParty();
			}
		}

		return null;
	}

	public static SAMLRelyingPartyType getSAMLRelyingPartyRegistration(ServiceConfig serviceConfig, String registrationId) {
		List<SAMLRelyingPartyType> registrations = getSAMLRelyingPartyRegistrations(serviceConfig);

		if (registrations != null) {
			for (SAMLRelyingPartyType registration : registrations) {
				if (registration.getId().equals(registrationId)) {
					return registration;
				}
			}
		}

		return null;
	}

	public static boolean isSsoAvailable(ServiceConfig serviceConfig) {
		List<SAMLRelyingPartyType> samlRegistrations = getSAMLRelyingPartyRegistrations(serviceConfig);

		return (samlRegistrations != null && samlRegistrations.size() > 0);
	}

	public static String getUIBaseUrl(TenantBindingType tenantBinding) {
		UIConfig uiConfig = tenantBinding.getUiConfig();

		if (uiConfig != null) {
			return uiConfig.getBaseUrl();
		}

		return null;
	}

	public static String getUILoginSuccessUrl(TenantBindingType tenantBinding) throws MalformedURLException {
		UIConfig uiConfig = tenantBinding.getUiConfig();

		if (uiConfig != null) {
			return uiConfig.getBaseUrl() + uiConfig.getLoginSuccessUrl();
		}

		return null;
	}

	public static String getUIAuthorizationSuccessUrl(TenantBindingType tenantBinding) throws MalformedURLException {
		UIConfig uiConfig = tenantBinding.getUiConfig();

		if (uiConfig != null) {
			return uiConfig.getBaseUrl() + uiConfig.getAuthorizationSuccessUrl();
		}

		return null;
	}

	public static String getUILogoutSuccessUrl(TenantBindingType tenantBinding) throws MalformedURLException {
		UIConfig uiConfig = tenantBinding.getUiConfig();

		if (uiConfig != null) {
			return uiConfig.getBaseUrl() + uiConfig.getLogoutSuccessUrl();
		}

		return null;
	}
}
