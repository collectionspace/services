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

package org.collectionspace.services.authorization.spring;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.request.DefaultOAuth2RequestFactory;

/**
 * An OAuth2RequestFactory that expects the password to be base64 encoded. This implementation
 * copies the parameters, decodes the password if present, and passes the result to
 * DefaultOAuth2RequestFactory.
 */
public class CSpaceOAuth2RequestFactory extends DefaultOAuth2RequestFactory {
    private final String PASSWORD_PARAMETER = "password";
    
    public CSpaceOAuth2RequestFactory(ClientDetailsService clientDetailsService) {
        super(clientDetailsService);
    }

    @Override
    public AuthorizationRequest createAuthorizationRequest(
            Map<String, String> authorizationParameters) {
        return super.createAuthorizationRequest(decodePassword(authorizationParameters));
    }

    @Override
    public TokenRequest createTokenRequest(
            Map<String, String> requestParameters,
            ClientDetails authenticatedClient) {
        return super.createTokenRequest(decodePassword(requestParameters), authenticatedClient);
    }
    
    private Map<String, String> decodePassword(Map<String, String> parameters) {
        if (parameters.containsKey(PASSWORD_PARAMETER)) {
            String base64EncodedPassword = parameters.get(PASSWORD_PARAMETER);
            String password = new String(DatatypeConverter.parseBase64Binary(base64EncodedPassword), StandardCharsets.UTF_8);

            Map<String, String> parametersCopy = new HashMap<String, String>(parameters);

            parametersCopy.put(PASSWORD_PARAMETER, password);

            return parametersCopy;
        }

        return parameters;
    }
}
