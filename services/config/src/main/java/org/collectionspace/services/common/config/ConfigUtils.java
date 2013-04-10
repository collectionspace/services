package org.collectionspace.services.common.config;

import java.util.ArrayList;
import java.util.List;

import org.collectionspace.services.config.tenant.RepositoryDomainType;
import org.collectionspace.services.config.tenant.TenantBindingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigUtils {
    final static Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
    
    // Default database names
    public static String DEFAULT_CSPACE_DATABASE_NAME = "cspace";
    public static String DEFAULT_NUXEO_REPOSITORY_NAME = "default";
    public static String DEFAULT_NUXEO_DATABASE_NAME = "nuxeo";
    
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
			logger.error(String.format("No domain name was specified on call to getRepositoryName() method."));
		}
		
		if (result == null && logger.isTraceEnabled()) {
			logger.trace(String.format("Could not find the repository name for tenent name='%s' and domain='%s'",
					tenantBindingType.getName(), domainName));
		}
		
		return result;
	}
    
}
