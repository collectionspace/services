package org.collectionspace.services.common;

/**
 * Defines a compound key consisting of the combination of a tenant ID and a
 * Nuxeo document type (docType), for accessing entries in the
 * UriTemplateRegistry.
 */
public class UriTemplateRegistryKey {

    private String tenantId;
    private String docType;
    
    public UriTemplateRegistryKey() {
    }

    public UriTemplateRegistryKey(String tenantId, String docType) {
        this.tenantId = tenantId;
        this.docType = docType;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the docType
     */
    public String getDocType() {
        return docType;
    }

    /**
     * @param docType the docType to set
     */
    public void setDocType(String docType) {
        this.docType = docType;
    }

    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }

        if (o == this) {
            return true;
        }

        // Cast the compared-to object to an object of this type
        UriTemplateRegistryKey key = (UriTemplateRegistryKey) o;

        // FIXME: Look into possible case-sensitivity issues for registry keys - ADR 2012-08-15
        
        // If either the tenant ID or docType values don't match,
        // whether only one is null, or via a failure of an 'equals'
        // test of their values, return false
        if (tenantId == null ? key.tenantId != null : !tenantId.equals(key.tenantId)) {
            return false;
        }
        if (docType == null ? key.docType != null : !docType.equals(key.docType)) {
            return false;
        }
    
        return true;

    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.tenantId != null ? this.tenantId.hashCode() : 0);
        hash = 89 * hash + (this.docType != null ? this.docType.hashCode() : 0);
        return hash;
    }
}
