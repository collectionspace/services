package org.collectionspace.authentication;

import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * A CollectionSpace user. This class implements the Spring UserDetails interface,
 * but the enabled, accountNonExpired, credentialsNonExpired, and accountNonLocked
 * properties are not meaningful and will always be true. CollectionSpace users
 * may be disabled (aka inactive), but this check is done outside of Spring Security,
 * after Spring authentication has succeeded.
 * 
 * @See org.collectionspace.services.common.security.SecurityInterceptor.
 */
public class CSpaceUser extends User {
    
    private static final long serialVersionUID = 3326192720134327612L;

    private Set<CSpaceTenant> tenants;
    private CSpaceTenant primaryTenant;
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
            Set<CSpaceTenant> tenants,
            Set<? extends GrantedAuthority> authorities) {

        super(username, password,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);

        this.tenants = tenants;
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
    
}
