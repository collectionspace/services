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

import java.security.MessageDigest;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.authentication.AuthN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.jboss.crypto.digest.DigestCallback;
import org.jboss.security.Base64Encoder;
import org.jboss.security.Base64Utils;

/**
 *
 * @author 
 */
public class SecurityUtils {

	private static final String ADMIN_TENANT_ID = "0";
    private static final Logger logger = LoggerFactory.getLogger(SecurityUtils.class);
    public static final String URI_PATH_SEPARATOR = "/";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 24;
    
    public static final String BASE64_ENCODING = "BASE64";
    public static final String BASE16_ENCODING = "HEX";
    public static final String RFC2617_ENCODING = "RFC2617";
    private static char MD5_HEX[] = "0123456789abcdef".toCharArray();
    

    /**
     * createPasswordHash creates password has using configured digest algorithm
     * and encoding
     * @param user
     * @param password in cleartext
     * @return hashed password
     */
    public static String createPasswordHash(String username, String password) {
        //TODO: externalize digest algo and encoding
        return createPasswordHash("SHA-256", //digest algo
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
        if (password == null) {
            String msg = "Password missing ";
            logger.error(msg);
            throw new IllegalArgumentException(msg);
        }
        if (password.length() < MIN_PASSWORD_LENGTH 
        		|| password.length() > MAX_PASSWORD_LENGTH) {
            String msg = "Bad password: '"+password+"': length should be >= "
            		+ MIN_PASSWORD_LENGTH + " and <= " + MAX_PASSWORD_LENGTH;
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
    	boolean result = false;
    	
    	String tenantId = null;
    	try {
    		tenantId = AuthN.get().getCurrentTenantId();
    	} catch (Throwable e) {
    		tenantId = ADMIN_TENANT_ID;
    	}
    	
        if (tenantId != null) {
            if (ADMIN_TENANT_ID.equals(tenantId) == true) {
                result = true;
            }
        }
        
        return result;
    }
    
    public static String createPasswordHash(String hashAlgorithm, String hashEncoding, String hashCharset, String username, String password)
    {
        return createPasswordHash(hashAlgorithm, hashEncoding, hashCharset, username, password, null);
    }

    public static String createPasswordHash(String hashAlgorithm, String hashEncoding, String hashCharset, String username, String password, DigestCallback callback)
    {
        String passwordHash = null;
        byte passBytes[];
        try
        {
            if(hashCharset == null)
                passBytes = password.getBytes();
            else
                passBytes = password.getBytes(hashCharset);
        }
        catch(UnsupportedEncodingException uee)
        {
            logger.error((new StringBuilder()).append("charset ").append(hashCharset).append(" not found. Using platform default.").toString(), uee);
            passBytes = password.getBytes();
        }
        try
        {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            if(callback != null)
                callback.preDigest(md);
            md.update(passBytes);
            if(callback != null)
                callback.postDigest(md);
            byte hash[] = md.digest();
            if(hashEncoding.equalsIgnoreCase("BASE64"))
                passwordHash = encodeBase64(hash);
            else
            if(hashEncoding.equalsIgnoreCase("HEX"))
                passwordHash = encodeBase16(hash);
            else
            if(hashEncoding.equalsIgnoreCase("RFC2617"))
                passwordHash = encodeRFC2617(hash);
            else
                logger.error((new StringBuilder()).append("Unsupported hash encoding format ").append(hashEncoding).toString());
        }
        catch(Exception e)
        {
            logger.error("Password hash calculation failed ", e);
        }
        return passwordHash;
    }

    public static String encodeRFC2617(byte data[])
    {
        char hash[] = new char[32];
        for(int i = 0; i < 16; i++)
        {
            int j = data[i] >> 4 & 0xf;
            hash[i * 2] = MD5_HEX[j];
            j = data[i] & 0xf;
            hash[i * 2 + 1] = MD5_HEX[j];
        }

        return new String(hash);
    }

    public static String encodeBase16(byte bytes[])
    {
        StringBuffer sb = new StringBuffer(bytes.length * 2);
        for(int i = 0; i < bytes.length; i++)
        {
            byte b = bytes[i];
            char c = (char)(b >> 4 & 0xf);
            if(c > '\t')
                c = (char)((c - 10) + 97);
            else
                c += '0';
            sb.append(c);
            c = (char)(b & 0xf);
            if(c > '\t')
                c = (char)((c - 10) + 97);
            else
                c += '0';
            sb.append(c);
        }

        return sb.toString();
    }

    public static String encodeBase64(byte bytes[])
    {
        String base64 = null;
        try
        {
            base64 = Base64Encoder.encode(bytes);
        }
        catch(Exception e) { }
        return base64;
    }

    public static String tob64(byte buffer[])
    {
        return Base64Utils.tob64(buffer);
    }

    public static byte[] fromb64(String str)
        throws NumberFormatException
    {
        return Base64Utils.fromb64(str);
    }

    
}
