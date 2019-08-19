/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *//**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.authentication.spring;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountNotFoundException;

import org.collectionspace.authentication.CSpaceTenant;
import org.collectionspace.authentication.CSpaceUser;
import org.collectionspace.authentication.realm.CSpaceRealm;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * A Spring UserDetailsService for CollectionSpace.
 */
public class CSpaceUserDetailsService implements UserDetailsService {
    private CSpaceRealm realm = null;

    public CSpaceUserDetailsService(CSpaceRealm realm) {
        this.realm = realm;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String password = null;
        String salt = null;
        Set<CSpaceTenant> tenants = null;
        Set<GrantedAuthority> grantedAuthorities = null;
        
        try {
            password = realm.getPassword(username);
            salt = realm.getSalt(username);
            tenants = getTenants(username);
            grantedAuthorities = getAuthorities(username);
        }
        catch (AccountNotFoundException e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }
        catch (AccountException e) {
            throw new AuthenticationServiceException(e.getMessage(), e);
        }
        
        CSpaceUser cspaceUser = 
            new CSpaceUser(
                username,
                password,
                salt,
                tenants,
                grantedAuthorities);
                
        return cspaceUser;
    }
    
    protected Set<GrantedAuthority> getAuthorities(String username) throws AccountException {
        Set<String> roles = realm.getRoles(username);
        Set<GrantedAuthority> authorities = new LinkedHashSet<GrantedAuthority>(roles.size());
        
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }
        
        return authorities;
    }
    
    protected Set<CSpaceTenant> getTenants(String username) throws AccountException {
        Set<CSpaceTenant> tenants = realm.getTenants(username);
        
        return tenants;
    }
}
