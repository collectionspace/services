package org.collectionspace.authentication;

import java.util.Set;

import org.collectionspace.authentication.jackson2.CSpaceUserDeserializer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * A CollectionSpace user. This class implements the Spring UserDetails interface,
 * but the enabled, accountNonExpired, credentialsNonExpired, and accountNonLocked
 * properties are not meaningful and will always be true. CollectionSpace users
 * may be disabled (aka inactive), but this check is done outside of Spring Security,
 * after Spring authentication has succeeded.
 *
 * @See org.collectionspace.services.common.security.SecurityInterceptor.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
@JsonDeserialize(using = CSpaceUserDeserializer.class)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CSpaceUser extends User {

    private static final long serialVersionUID = 3326192720134327612L;

    private Set<CSpaceTenant> tenants;
    private CSpaceTenant primaryTenant;
    private boolean requireSSO;
    private String ssoId;
    private String salt;

    /**
     * Creates a CSpaceUser with the given username, hashed password, associated
     * tenants, and granted authorities.
     *
     * @param username the username, e.g. "admin@core.collectionspace.org"
     * @param password the hashed password, e.g. "59PnafP1k9rcuGNMxbCfyQ3TphxKBqecsJI2Yv5vrms="
     * @param tenants the tenants associated with the user
     * @param authorities the authorities that have been granted to the user
     */
    public CSpaceUser(String username, String password, String salt,
            String ssoId,
            boolean requireSSO,
            Set<CSpaceTenant> tenants,
            Set<? extends GrantedAuthority> authorities) {

        super(username, password,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);

        this.tenants = tenants;
        this.ssoId = ssoId;
        this.requireSSO = requireSSO;
        this.salt = salt;

        if (!tenants.isEmpty()) {
            primaryTenant = tenants.iterator().next();
        }
    }

    /**
     * Retrieves the tenants associated with the user.
     *
     * @return the tenants
     */
    public Set<CSpaceTenant> getTenants() {
        return tenants;
    }

    /**
     * Retrieves the primary tenant associated with the user.
     *
     * @return the tenants
     */
    public CSpaceTenant getPrimaryTenant() {
        return primaryTenant;
    }

    /**
     * Returns a "salt" string to use when encrypting a user's password
     * @return
     */
    public String getSalt() {
    	return salt != null ? salt : "";
    }

    /**
     * Returns the ID from the user's SSO provider, if the user signed in via SSO
     * @return the SSO ID
     */
    public String getSsoId() {
        return ssoId;
    }

    /**
     * Sets the ID from the user's SSO provider.
     */
    public void setSsoId(String ssoId) {
        this.ssoId = ssoId;
    }

    /**
     * Determines if the user is required to log in using single sign-on.
     * @return true if SSO is required, false otherwise
     */
    public boolean isRequireSSO() {
        return requireSSO;
    }
}
