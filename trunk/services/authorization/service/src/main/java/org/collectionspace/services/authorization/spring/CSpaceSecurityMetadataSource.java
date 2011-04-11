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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.authorization.spring;

import java.util.Collection;
import java.util.Properties;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;

/**
 *
 * @author 
 */
public class CSpaceSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {

    private Properties urlProperties;

    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    public Collection<ConfigAttribute> getAttributes(Object filter)
            throws IllegalArgumentException {
        FilterInvocation filterInvocation = (FilterInvocation) filter;
        String url = filterInvocation.getRequestUrl();

        //get the roles for requested page from the property file
        String urlPropsValue = urlProperties.getProperty(url);
        StringBuilder rolesStringBuilder = new StringBuilder();
        if (urlPropsValue != null) {
            rolesStringBuilder.append(urlPropsValue).append(",");
        }

        if (!url.endsWith("/")) {
            int lastSlashIndex = url.lastIndexOf("/");
            url = url.substring(0, lastSlashIndex + 1);
        }


        String[] urlParts = url.split("/");

        StringBuilder urlBuilder = new StringBuilder();
        for (String urlPart : urlParts) {
            if (urlPart.trim().length() == 0) {
                continue;
            }
            urlBuilder.append("/").append(urlPart);
            urlPropsValue = urlProperties.getProperty(urlBuilder.toString() + "/**");

            if (urlPropsValue != null) {
                rolesStringBuilder.append(urlPropsValue).append(",");
            }
        }

        if (rolesStringBuilder.toString().endsWith(",")) {
            rolesStringBuilder.deleteCharAt(rolesStringBuilder.length() - 1);
        }


        if (rolesStringBuilder.length() == 0) {
            return null;
        }

        return SecurityConfig.createListFromCommaDelimitedString(rolesStringBuilder.toString());
    }

    public boolean supports(Class<?> arg0) {
        return true;
    }

    public void setUrlProperties(Properties urlProperties) {
        this.urlProperties = urlProperties;
    }

    public Properties getUrlProperties() {
        return urlProperties;
    }
}
