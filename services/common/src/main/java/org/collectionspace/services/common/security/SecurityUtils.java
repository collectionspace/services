/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.common.security;

import java.util.List;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.authentication.AuthN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author 
 */
public class SecurityUtils {

    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    public static final String URI_PATH_SEPARATOR = "/";

    /**
     * createPasswordHash creates password has using configured digest algorithm
     * and encoding
     * @param user
     * @param password in cleartext
     * @return hashed password
     */
    public static String createPasswordHash(String username, String password) {
        //TODO: externalize digest algo and encoding
        return org.jboss.security.Util.createPasswordHash("SHA-256", //digest algo
                "base64", //encoding
                null, //charset
                username,
                password);
    }

    /**
     * validatePassword validates password per configured password policy
     * @param password
     */
    public static void validatePassword(String password) {
        //TODO: externalize password length
        if (password == null) {
            String msg = "Password missing ";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (password.length() < 8 || password.length() > 24) {
            String msg = "Password length should be >8 and <24";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
    }

	/**
	 * Gets the resource name.
	 *
	 * @param uriInfo the uri info
	 * @return the resource name
	 */
	public static String getResourceName(UriInfo uriInfo) {
		String uriPath = uriInfo.getPath();

		MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
		
		for (String pathParamName : pathParams.keySet()) {
			//assumption : path params for csid for any entity has substring csid in name
			String pathParamValue = pathParams.get(pathParamName).get(0);
			if ((pathParamName.toLowerCase().indexOf("csid") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
			if ((pathParamName.toLowerCase().indexOf("predicate") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
			if (pathParamName.toLowerCase().indexOf("specifier") > -1) {
				//replace name and specifiers with wildcard
				uriPath = uriPath.replace("urn:cspace:name(" + pathParamValue
						+ ")", "*");
			}
			if ((pathParamName.toLowerCase().indexOf("ms") > -1)) {
				//replace csids with wildcard
				uriPath = uriPath.replace(pathParamValue, "*");
			}
		}
		
		// FIXME: REM
		// Since the hjid (HyperJaxb3 generated IDs are not unique strings in URIs that also have a CSID,
		// we need to replace hjid last.  We can fix this by having HyperJaxb3 generate UUID.
		// Assumption : path param name for csid is lowercase
		//
		List<String> hjidValueList = pathParams.get("id");
		if (hjidValueList != null) {
			String hjidValue = hjidValueList.get(0); //should be just one value, so get the first.
			uriPath = uriPath.replace(hjidValue, "*");
		}
		
		uriPath = uriPath.replace("//", "/");
		return uriPath;
	}
    
	/**
	 * Gets the resource name.
	 *
	 * @param uriInfo the uri info
	 * @return the resource name
	 */
	public static String getResourceEntity(UriInfo uriInfo) {
		String result = null;
		
		result = getResourceEntity(uriInfo.getPath());
//		List<PathSegment> pathSegmentList = uriInfo.getPathSegments();
//		if (pathSegmentList.isEmpty() == false) {
//			result = pathSegmentList.get(0).getPath();
//		}
		
		return result;
	}
	
	/**
	 * Gets the resource entity by returning the first segment of the resource path
	 *
	 * @param relativePath the relative path
	 * @return the resource entity
	 * @throws URISyntaxException the uRI syntax exception
	 */
	public static String getResourceEntity(String relativePath)
	{
		String result = "";
		
	    StringTokenizer strTok = new StringTokenizer(relativePath, URI_PATH_SEPARATOR);
	    String pathSegment = null;
	    while (strTok.hasMoreTokens() == true) {
	    	pathSegment = strTok.nextToken();
	    	if (pathSegment.equals("*") == true) {
	    		//
	    		// leave the loop if we hit a wildcard character
	    		//
	    		break;
	    	}
	    	if (result.length() > 0) {
	    		result = result.concat(URI_PATH_SEPARATOR);
	    	}
	    	result = result.concat(pathSegment);
	    }
		
		return result;
	}
    
    /**
     * Checks if is entity is action as a proxy for all sub-resources.
     *
     * @return true, if is entity proxy is acting as a proxy for all sub-resources
     */
    public static final boolean isEntityProxy() {
    	//
    	// should be getting this information from  the cspace config settings (tenent bindings file).
    	return true;
    }

    
    /**
     * isCSpaceAdmin check if authenticated user is a CSpace administrator
     * @param tenantId
     * @return
     */
    public static boolean isCSpaceAdmin() {
        String tenantId = AuthN.get().getCurrentTenantId();
        if (tenantId != null) {
            if (!"0".equals(tenantId)) {
                return false;
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
