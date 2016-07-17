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
    
    /**
     * Creates a CSpaceUser with the given username, hashed password, associated
     * tenants, and granted authorities.
     * 
     * @param username the username, e.g. "admin@core.collectionspace.org"
     * @param password the hashed password, e.g. "59PnafP1k9rcuGNMxbCfyQ3TphxKBqecsJI2Yv5vrms="
     * @param tenants the tenants associated with the user
     * @param authorities the authorities that have been granted to the user
     */
    public CSpaceUser(String username, String password,
            Set<CSpaceTenant> tenants,
            Set<? extends GrantedAuthority> authorities) {

        super(username, password,
                true, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);

        this.tenants = tenants;
    }

    /**
     * Retrieves the tenants associated with the user.
     * 
     * @return the tenants
     */
    public Set<CSpaceTenant> getTenants() {
        return tenants;
    }
}
