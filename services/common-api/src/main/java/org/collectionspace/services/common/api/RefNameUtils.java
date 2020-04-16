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
 *  limitations under the license.
 */
package org.collectionspace.services.common.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RefNameUtils is a collection of utilities related to refName URN strings
 * refNames are URNs that reference a document entity, often an authority or 
 * authority term. They are strings that take the form (for authorities):
 * urn:cspace:org.collectionspace.demo:vocabulary:name(Entry Methods)'Entry Methods'
 * or the form (for authority terms):
 * urn:cspace:org.collectionspace.demo:vocabulary:name(Entry Methods):item:name(Loan)'Loan'
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public class RefNameUtils {

    private final Logger logger = LoggerFactory.getLogger(RefNameUtils.class);

    public static final String SEPARATOR = ":";
    public static final String URN_PREFIX = "urn";
    public static final String URN_CSPACE = "cspace";
    public static final String URN_CSPACE_PREFIX = URN_PREFIX + SEPARATOR + URN_CSPACE + SEPARATOR; // "urn:cspace:"
    public static final int URN_PREFIX_LEN = URN_CSPACE_PREFIX.length();
    public static final String URN_NAME_PREFIX = "urn:cspace:name(";
    public static final int URN_NAME_PREFIX_LEN = URN_NAME_PREFIX.length();
    public static final String NAME_SPECIFIER = "name";
    public static final String ID_SPECIFIER = "id";
    
	// FIXME Should not be hard-coded
    private static final String ITEMS_REGEX = "item|person|organization";
    // In a list of tokens, these are indices for each part
    private static final int DOMAIN_TOKEN = 0; 			// e.g., 'org.collectionspace.demo'
    private static final int RESOURCE_TOKEN = 1; 		// vocabulary, personauthority, etc.
    private static final int AUTH_INSTANCE_TOKEN = 2;	// name(Entry Methods)'Entry Methods'
    private static final int ITEMS_TOKEN = 3;			// 'item', 'person', etc.
    private static final int ITEM_INSTANCE_TOKEN = 4;	// name(Entry Methods)'Entry Methods'
    private static final int AUTH_REFNAME_TOKENS = 3;	// domain, resource, auth
    private static final int AUTH_ITEM_REFNAME_TOKENS = 5;	// domain, resource, auth, "items", item
    // Tokenizing the INSTANCE, these are indices for each item-part
    private static final int INSTANCE_SPEC_TYPE_TOKEN = 0;	// 'name' or 'id' 
    private static final int INSTANCE_SPEC_TOKEN = 1; 		// name or id value
    private static final int INSTANCE_DISPLAYNAME_TOKEN = 2;// optional displayName suffix
    private static final int INSTANCE_TOKENS_MIN = 2;
    private static final int INSTANCE_TOKENS_MAX = 3;
    
	public static String domainToPhrase(String domain) {
		String result = "";
		
		String[] split = domain.split("\\.", 0);
		for (String token : split) {
			result = result + token + ' ';
		}
		
		return result.trim();
	}

    public static class AuthorityInfo {
        private final Logger logger = LoggerFactory.getLogger(AuthorityInfo.class);
    	private static int MIN_TOKENS = 3;
        public String domain;
        public String resource;
        public String csid;
        public String name; // Is this the short ID?
        public String displayName;
        
        public AuthorityInfo(String refNameTokens[]) throws IllegalArgumentException {
        	try {
	        	if(refNameTokens.length < MIN_TOKENS) {
	        		throw new IllegalArgumentException("Malformed refName for Authority (too few tokens)");
	        	}
	        	this.domain = refNameTokens[DOMAIN_TOKEN]; 
	        	this.resource = refNameTokens[RESOURCE_TOKEN];
	        	String idTokens[] = refNameTokens[AUTH_INSTANCE_TOKEN].split("[()]", INSTANCE_TOKENS_MAX);
	        	if(idTokens.length<INSTANCE_TOKENS_MIN) {
	        		throw new IllegalArgumentException("Missing/malformed identifier");
	        	}
	        	if(idTokens[INSTANCE_SPEC_TYPE_TOKEN].equals(NAME_SPECIFIER)) {
	        		this.name = idTokens[INSTANCE_SPEC_TOKEN];
	        		this.csid = null;
	        	} else if(idTokens[INSTANCE_SPEC_TYPE_TOKEN].startsWith(ID_SPECIFIER)) {
	        		this.csid = idTokens[INSTANCE_SPEC_TOKEN];
	        		this.name = null;
	        	} else {
	        		throw new IllegalArgumentException("Identifier type must be '"
	        						+NAME_SPECIFIER+"' or '"+ID_SPECIFIER+"'");
	        	}
	        	// displayName is always in quotes, so must have at least 3 chars
	    		this.displayName = 
	    			((idTokens.length<INSTANCE_TOKENS_MAX)||(idTokens[INSTANCE_DISPLAYNAME_TOKEN].length()<3))? null:
	    			idTokens[INSTANCE_DISPLAYNAME_TOKEN].substring(1, idTokens[INSTANCE_DISPLAYNAME_TOKEN].length()-1);
        	} catch (IllegalArgumentException e) {
	            if (logger.isDebugEnabled()) {
	            	logger.debug("Problem Building AuthorityInfo from tokens: " 
	            			+ RefNameUtils.implodeStringArray(refNameTokens, ", "));
	            }
	            throw e;
        	}
        }
        
        public String getRelativeUri() {
        	StringBuilder uri = new StringBuilder();
        	// FIXME This should not be hard-coded -see https://issues.collectionspace.org/browse/CSPACE-5987
        	if(resource.equals("vocabulary")) {
        		uri.append("/vocabularies/");
        	} else if(resource.equals("personauthority")) {
        		uri.append("/personauthorities/");
        	} else if(resource.equals("orgauthority")) {
        		uri.append("/orgauthorities/");
        	} else {
        		if(!(resource.equals("orgauthorities")
        			|| resource.equals("personauthorities")
        			|| resource.equals("locationauthorities")
        			|| resource.equals("placeauthorities")
        			|| resource.equals("vocabularies"))) {	
        			logger.warn("Unrecognized Authority Type: " + resource);
        		}
        		uri.append("/"+resource+"/");
        	}
        	if(csid!=null) {
        		uri.append(csid);
        	} else if(name!=null) {
        		uri.append("urn:cspace:name("+name+")");
        	} else {
        		throw new RuntimeException("Missing id/name specifier");
        	}
        	return uri.toString();
        }
    };

    public static class AuthorityTermInfo {
        private final Logger logger = LoggerFactory.getLogger(AuthorityTermInfo.class);
    	private static int MIN_TOKENS = 5;
    	public AuthorityInfo inAuthority;
    	public String csid;
    	public String name;
    	public String displayName;
        
        public AuthorityTermInfo(String refNameTokens[]) throws IllegalArgumentException {
        	try {
	        	if(refNameTokens.length < MIN_TOKENS) {
	        		throw new IllegalArgumentException("Malformed refName for AuthorityTerm (too few tokens)");
	        	}
	        	this.inAuthority = new AuthorityInfo(refNameTokens); 
	        	if(!refNameTokens[ITEMS_TOKEN].matches(ITEMS_REGEX)) {
	        		throw new IllegalArgumentException("Item spec must be one of: "+ITEMS_REGEX);
	        	}
	        	String idTokens[] = refNameTokens[ITEM_INSTANCE_TOKEN].split("[\\(\\)]", 3);
	        	if(idTokens.length<INSTANCE_TOKENS_MIN) {
	        		throw new IllegalArgumentException("Missing/malformed identifier");
	        	}
	        	if(idTokens[INSTANCE_SPEC_TYPE_TOKEN].equals(NAME_SPECIFIER)) {
	        		this.name = idTokens[INSTANCE_SPEC_TOKEN];
	        		this.csid = null;
	        	} else if(idTokens[INSTANCE_SPEC_TYPE_TOKEN].startsWith(ID_SPECIFIER)) {
	        		this.csid = idTokens[INSTANCE_SPEC_TOKEN];
	        		this.name = null;
	        	} else {
	        		throw new IllegalArgumentException("Identifier type must be 'name' or 'id'");
	        	}
	        	// displayName is always in quotes, so must have at least 3 chars
	        	this.displayName = 
	        		((idTokens.length<INSTANCE_TOKENS_MAX)||(idTokens[INSTANCE_DISPLAYNAME_TOKEN].length()<3))? null:
	    			idTokens[INSTANCE_DISPLAYNAME_TOKEN].substring(1, idTokens[INSTANCE_DISPLAYNAME_TOKEN].length()-1);
        	} catch (IllegalArgumentException e) {
	            if (logger.isDebugEnabled()) {
	            	logger.debug("Problem Building AuthorityTermInfo from tokens: " 
	            			+ RefNameUtils.implodeStringArray(refNameTokens, ", "));
	            }
	            throw e;
        	}
        }
        
        public String getRelativeUri() {
        	StringBuilder uri = new StringBuilder(inAuthority.getRelativeUri()+"/items/");
        	if(csid!=null) {
        		uri.append(csid);
        	} else if(name!=null) {
        		uri.append("urn:cspace:name("+name+")");
        	} else {
        		throw new RuntimeException("Missing id/name specifier");
        	}
        	return uri.toString();
        }
    };

    public static AuthorityInfo parseAuthorityInfo(String refName)
            throws IllegalArgumentException {
    	if(refName==null || !refName.startsWith(URN_CSPACE_PREFIX))
    		throw new IllegalArgumentException( "Null or invalid refName syntax");
    	String[] refNameTokens = refName.substring(URN_PREFIX_LEN).split(SEPARATOR, AUTH_REFNAME_TOKENS);
    	return new AuthorityInfo(refNameTokens);
    }

	public static AuthorityTermInfo parseAuthorityTermInfo(String refName) throws IllegalArgumentException {
		if (refName == null || !refName.startsWith(URN_CSPACE_PREFIX)) {
			throw new IllegalArgumentException("Null or invalid refName syntax");
		}
		String[] refNameTokens = refName.substring(URN_PREFIX_LEN).split(SEPARATOR, AUTH_ITEM_REFNAME_TOKENS);
		return new AuthorityTermInfo(refNameTokens);
	}

    public static String stripAuthorityTermDisplayName(String refName)
            throws IllegalArgumentException {
    	if(refName==null || !refName.startsWith(URN_CSPACE_PREFIX))
    		throw new IllegalArgumentException( "Null or invalid refName syntax");
    	String[] refNameTokens = refName.substring(URN_PREFIX_LEN).split(SEPARATOR, AUTH_ITEM_REFNAME_TOKENS);
    	int rightParen = refNameTokens[ITEM_INSTANCE_TOKEN].indexOf(')');
    	refNameTokens[ITEM_INSTANCE_TOKEN] = refNameTokens[ITEM_INSTANCE_TOKEN].substring(0, rightParen+1);
    	return URN_CSPACE_PREFIX + implodeStringArray(refNameTokens, SEPARATOR);
    }

    public static String implodeStringArray(String tokens[], String separator) {
    	if (tokens.length==0) {
    		return "";
    	} else {
    		StringBuffer sb = new StringBuffer();
    		sb.append(tokens[0]);
    		for (int i=1;i<tokens.length;i++) {
    			sb.append(separator);
    			sb.append(tokens[i]);
    		}
    		return sb.toString();
    	}
    }
    
    public static boolean isTermRefname(String specifier) {
    	boolean result = true;
    	
    	try {
    		AuthorityTermInfo authorityTermInfo = RefNameUtils.parseAuthorityTermInfo(specifier);
    	} catch (Exception e) {
    		result = false;
    	}

    	return result;
    }

    /*
     * Returns the name / shortIdentifier value of an authority item in a refName
     */
    public static String getItemShortId(String refName) {
        String name = "";
        try {
        	String[] refNameTokens = refName.substring(URN_PREFIX_LEN).split(SEPARATOR, AUTH_ITEM_REFNAME_TOKENS);
            AuthorityTermInfo authTermInfo = new AuthorityTermInfo(refNameTokens);
            name = authTermInfo.name;
        } catch(Exception e) {
            // do nothing
        }
        return name;
    }
    
    
    /**
     * Extracts the display name from a refName. The refName may either be for an
     * authority term, or an authority/procedure.
     *
     * @param  refName The refName
     * @return The display name contained in the refName
     */
    public static String getDisplayName(String refName) throws IllegalArgumentException {
    	String displayName = null;
    	
    	try {
    		AuthorityTermInfo authorityTermInfo = parseAuthorityTermInfo(refName);
    		displayName = authorityTermInfo.displayName;
    	}
    	catch(IllegalArgumentException invalidAuthorityTermRefNameException) {
        	try {   		
        		AuthorityInfo authorityInfo = parseAuthorityInfo(refName);
        		displayName = authorityInfo.displayName;
        	}
        	catch(IllegalArgumentException invalidRefNameException) {
        		throw new IllegalArgumentException("Invalid refName");
        	}
    	}
    	
    	return displayName;
    }    
}

