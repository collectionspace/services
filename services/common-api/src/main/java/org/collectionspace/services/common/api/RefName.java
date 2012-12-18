package org.collectionspace.services.common.api;

import org.collectionspace.services.common.api.RefNameUtils.AuthorityInfo;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Usage for this class, if you have a URN and would like to get at its fields,
 * is to call one of these methods:
 *
 * RefName.AuthorityItem item =
 * RefName.AuthorityItem.parse(RefName.AUTHORITY_ITEM_EXAMPLE); or
 * RefName.Authority authority =
 * RefName.Authority.parse(RefName.AUTHORITY_EXAMPLE);
 *
 * From the object returned, you may set/get any of the public fields.
 *
 * If you want to format a string urn, then you need to construct either a
 * RefName.AuthorityItem or RefName.Authority. You can parse a URN to do so, as
 * shown above, or you can construct one with a constructor, setting its fields
 * afterwards. A better way is to use one of the build*() methods on this class:
 *
 * RefName.Authority authority2 = RefName.buildAuthority(tenantName,
 * serviceName, authorityShortIdentifier, authorityDisplayName);
 *
 * RefName.AuthorityItem item2 = RefName.buildAuthorityItem(authority2,
 * RefName.EX_itemShortIdentifier, RefName.EX_itemDisplayName);
 *
 * Note that authority2 is an object, not a String, and is passed in to
 * RefName.buildAuthorityItem().
 *
 * Then simply call toString() on the object:
 *
 * String authorityURN = authority2.toString();
 *
 * String itemURN = item2.toString();
 *
 * These test cases are kept up-to-date in
 *
 * org.collectionspace.services.common.api.test.RefNameTest
 *
 * User: laramie
 */
public class RefName {

    /**
     * The logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(RefName.class);
    public static final String URN_PREFIX = "urn:cspace:";
    public static final String URN_NAME_PREFIX = "urn:cspace:name";
    public static final String REFNAME = "refName";
    
    public static interface RefNameInterface {
    	public String toString();
    }

    public static class Authority implements RefNameInterface {

        public String tenantName = "";
        public String resource = "";
        public String csid = null;
        public String shortIdentifier = null;
        public String displayName = "";

        public static Authority parse(String urn) {
            Authority authority;
            try {
                RefNameUtils.AuthorityInfo authorityInfo =
                        RefNameUtils.parseAuthorityInfo(urn);
                authority = authorityFromAuthorityInfo(authorityInfo, true);
            } catch (IllegalArgumentException iae) {
                return null;
            }
            return authority;
        }

        public String getDisplayName() {
        	return this.getDisplayName();
        }
        
        public String getShortIdentifier() {
            return this.shortIdentifier;
        }

        public String getCSID() {
            return this.csid;
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other instanceof Authority) {
                Authority ao = (Authority) other;
                return (this.tenantName.equals(ao.tenantName)
                        && this.resource.equals(ao.resource)
                        && ((this.shortIdentifier != null &&
                        	this.shortIdentifier.equals(ao.shortIdentifier))
                        || (this.csid != null && this.csid.equals(ao.csid))));
            } else {
                return false;
            }
        }

        public String getRelativeUri() {
        	StringBuilder sb = new StringBuilder();
        	sb.append("/");
        	sb.append(resource);
        	sb.append("/");
        	if(csid!=null) {
            	sb.append(csid);
        	} else if(shortIdentifier!= null) {
            	sb.append(URN_NAME_PREFIX);
            	sb.append("(");
            	sb.append(shortIdentifier);
            	sb.append(")");
        	} else {
        		throw new NullPointerException("Authority has neither CSID nor shortID!");
        	}
            return sb.toString();
        }

        public String toString() {
            String displaySuffix = (displayName != null && (!displayName.isEmpty())) ? '\'' + displayName + '\'' : "";
            //return URN_PREFIX + tenantName + ':' + resource + ":" + "name" + "(" + shortIdentifier + ")" + displaySuffix;
        	StringBuilder sb = new StringBuilder();
        	sb.append(URN_PREFIX);
        	sb.append(tenantName);
        	sb.append(RefNameUtils.SEPARATOR);
        	sb.append(resource);
        	sb.append(RefNameUtils.SEPARATOR);
        	if(csid!=null) {
            	sb.append(RefNameUtils.ID_SPECIFIER);
            	sb.append("(");
            	sb.append(csid);
            	sb.append(")");
        	} else if(shortIdentifier!= null) {
            	sb.append(RefNameUtils.NAME_SPECIFIER);
            	sb.append("(");
            	sb.append(shortIdentifier);
            	sb.append(")");
        	} else {
        		throw new NullPointerException("Authority has neither CSID nor shortID!");
        	}
        	sb.append(displaySuffix);
            return sb.toString();
        }

        public static Authority buildAuthority(
        		String tenantName, 
        		String serviceName, 
        		String csid, 
        		String authorityShortIdentifier, 
        		String authorityDisplayName) {
            Authority authority = new Authority();
            authority.tenantName = tenantName;
            authority.resource = serviceName;
            if (Tools.notEmpty(authority.resource)) {
                authority.resource = authority.resource.toLowerCase();
            }
            authority.csid = csid;
            authority.shortIdentifier = authorityShortIdentifier;
            authority.displayName = authorityDisplayName;
            return authority;
        }
    }

    public static class AuthorityItem implements RefNameInterface {

        public Authority inAuthority;
        public String shortIdentifier = "";
        public String displayName = "";

        public static AuthorityItem parse(String urn) {
            AuthorityItem authorityItem = null;
            try {
                RefNameUtils.AuthorityTermInfo termInfo =
                        RefNameUtils.parseAuthorityTermInfo(urn);
                authorityItem = authorityItemFromTermInfo(termInfo);
            } catch (IllegalArgumentException iae) {
                return null;
            }
            return authorityItem;
        }

        public String getDisplayName() {
        	return this.displayName;
        }
        
        public String getParentShortIdentifier() {
            return this.inAuthority.shortIdentifier;
        }

        public String getShortIdentifier() {
            return this.shortIdentifier;
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other instanceof AuthorityItem) {
                AuthorityItem aio = (AuthorityItem) other;
                boolean ok = true;
                ok = ok && aio.inAuthority != null;
                ok = ok && aio.inAuthority.equals(this.inAuthority);
                ok = ok && aio.shortIdentifier.equals(this.shortIdentifier);
                ok = ok && aio.displayName.equals(this.displayName);
                return ok;
            } else {
                return false;
            }
        }

        public String getRelativeUri() {
            return inAuthority.getRelativeUri() + "/items/" + URN_NAME_PREFIX + "(" + shortIdentifier + ")";
        }

        public String toString() {
            String displaySuffix = (displayName != null && (!displayName.isEmpty())) ? '\'' + displayName + '\'' : "";
            Authority ai = inAuthority;
            if (ai == null) {
                return URN_PREFIX + "ERROR:inAuthorityNotSet: (" + shortIdentifier + ")" + displaySuffix;
            } else {
                String base = URN_PREFIX + ai.tenantName + ':' + ai.resource + ":" + "name" + "(" + ai.shortIdentifier + ")";
                String refname = base + ":item:name(" + shortIdentifier + ")" + displaySuffix;
                return refname;
            }
        }
    }

    public static AuthorityItem buildAuthorityItem(String tenantName, String serviceName, String authorityShortID,
            String itemShortID, String itemDisplayName) {
        Authority authority = Authority.buildAuthority(tenantName, serviceName, null, authorityShortID, "");
        return buildAuthorityItem(authority, itemShortID, itemDisplayName);
    }

    public static AuthorityItem buildAuthorityItem(String authorityRefName, String itemShortID, String itemDisplayName) {
        Authority authority = Authority.parse(authorityRefName);
        AuthorityItem item = buildAuthorityItem(authority, itemShortID, itemDisplayName);
        return item;
    }

    public static AuthorityItem buildAuthorityItem(Authority authority, String itemShortID, String itemDisplayName) {
        AuthorityItem item = new AuthorityItem();
        item.inAuthority = authority;
        item.shortIdentifier = itemShortID;
        item.displayName = itemDisplayName;
        return item;
    }

    /**
     * Use this method to avoid formatting any urn's outside of this unit;
     * Caller passes in a shortId, such as "TestAuthority", and method returns
     * the correct urn path element, without any path delimiters such as '/' so
     * that calling shortIdToPath("TestAuthority") returns
     * "urn:cspace:name(TestAuthority)", and then this value may be put into a
     * path, such as "/personauthorities/urn:cspace:name(TestAuthority)/items".
     */
    public static String shortIdToPath(String shortId) {
        return URN_NAME_PREFIX + '(' + shortId + ')';
    }

    /**
     * Glue to create an AuthorityTermInfo object, used in RefNameUtils, from the
     * highly similar AuthorityItem object, used in this class.
     *
     * @param termInfo an AuthorityTermInfo object
     * @return an AuthorityItem object
     */
    private static AuthorityItem authorityItemFromTermInfo(AuthorityTermInfo termInfo) {
        if (termInfo == null) {
            return null;
        }
        AuthorityItem authorityItem = new AuthorityItem();
        authorityItem.inAuthority =
                authorityFromAuthorityInfo(termInfo.inAuthority, false);
        if (termInfo.name != null
                && !termInfo.name.trim().isEmpty()) {
            authorityItem.shortIdentifier = termInfo.name;
        } else {
            authorityItem.shortIdentifier = termInfo.csid;
        }
        authorityItem.displayName = termInfo.displayName;
        return authorityItem;
    }

    /**
     * Glue to create an AuthorityInfo object, used in RefNameUtils, from the
     * highly similar Authority object, used in this class.
     *
     * @param authorityInfo an AuthorityInfo object
     * @param includeDisplayName true to include the display name during creation;
     *   false to exclude it.
     * @return an Authority object
     */
    private static Authority authorityFromAuthorityInfo(AuthorityInfo authorityInfo,
            boolean includeDisplayName) {
        if (authorityInfo == null) {
            return null;
        }
        Authority authority = new Authority();
        authority.tenantName = authorityInfo.domain;
        authority.resource = authorityInfo.resource;
        if (authorityInfo.name != null
                && !authorityInfo.name.trim().isEmpty()) {
            authority.shortIdentifier = authorityInfo.name;
        }
        if (authorityInfo.csid != null
                && !authorityInfo.csid.trim().isEmpty()) {
            authority.csid = authorityInfo.csid;
        }
        if (includeDisplayName) {
            authority.displayName = authorityInfo.displayName;
        }
        return authority;
    }
}
