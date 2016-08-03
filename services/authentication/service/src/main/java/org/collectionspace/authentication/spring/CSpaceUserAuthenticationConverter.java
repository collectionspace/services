package org.collectionspace.authentication.spring;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;

/**
 * Converter for CSpace user authentication information to and from Maps.
 * This is used to serialize/deserialize user information to/from JWTs.
 * When extracting the user authentication from a map, only the username
 * is required. The full user information is retrieved from a UserDetailsService.
 */
public class CSpaceUserAuthenticationConverter implements UserAuthenticationConverter {

    private UserDetailsService userDetailsService;

    /**
     * Creates a converter that uses the given UserDetailsService when extracting
     * the authentication information.
     * 
     * @param userDetailsService the UserDetailsService to use
     */
    public CSpaceUserAuthenticationConverter(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public Map<String, ?> convertUserAuthentication(Authentication userAuthentication) {
        // In extractAuthentication we use a UserDetailsService to look up
        // the user's roles and tenants, so there's no need to serialize
        // those. We just need the username.
        
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        
        response.put(USERNAME, userAuthentication.getName());
        
        return response;
    }

    @Override
    public Authentication extractAuthentication(Map<String, ?> map) {
        if (!map.containsKey(USERNAME) || userDetailsService == null) {
            return null;
        }
        
        String username = (String) map.get(USERNAME);

        try {
            UserDetails user = userDetailsService.loadUserByUsername(username);
            
            return new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities());
        }
        catch(UsernameNotFoundException e) {
            return null;
        }
    }
}
