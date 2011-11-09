package org.collectionspace.services.common.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Usage for this class, if you have a URN and would like to get at its fields, is to call one of these methods:
 *
 *   RefName.AuthorityItem item = RefName.AuthorityItem.parse(RefName.AUTHORITY_ITEM_EXAMPLE);
 * or
 *   RefName.Authority authority = RefName.Authority.parse(RefName.AUTHORITY_EXAMPLE);
 *
 * From the object returned, you may set/get any of the public fields.
 *
 * If you want to format a string urn, then you need to construct either a RefName.AuthorityItem  or RefName.Authority.
 * You can parse a URN to do so, as shown above, or you can construct one with a constructor, setting its fields afterwards.
 * A better way is to use one of the build*() methods on this class:
 *
 *      RefName.Authority authority2 = RefName.buildAuthority(tenantName, serviceName, authorityShortIdentifier, authorityDisplayName);
 *
 *      RefName.AuthorityItem item2 = RefName.buildAuthorityItem(authority2,
 *                                                               RefName.EX_itemShortIdentifier,
 *                                                               RefName.EX_itemDisplayName);
 *
 * Note that authority2 is an object, not a String, and is passed in to RefName.buildAuthorityItem().
 *
 * Then simply call toString() on the object:
 *
 *   String authorityURN = authority2.toString();
 *
 *   String itemURN = item2.toString();
 *
 * These test cases are kept up-to-date in
 *
 *   org.collectionspace.services.common.api.test.RefNameTest
 *
 * User: laramie
 */
public class RefName {
	
    /** The logger. */
    private static final Logger logger = LoggerFactory.getLogger(RefName.class);

    public static final String HACK_VOCABULARIES = "Vocabularies"; //TODO: get rid of these.
    public static final String HACK_ORGANIZATIONS = "Organizations"; //TODO: get rid of these.
    public static final String HACK_ORGAUTHORITIES = "Orgauthorities";  //TODO: get rid of these.
    public static final String HACK_PERSONAUTHORITIES = "Personauthorities";  //TODO: get rid of these.
    public static final String HACK_LOCATIONAUTHORITIES = "Locationauthorities";  //TODO: get rid of these.
    public static final String URN_PREFIX = "urn:cspace:";
    public static final String URN_NAME_PREFIX = "urn:cspace:name";
    public static final String REFNAME = "refName";
    public static final String AUTHORITY_REGEX = "urn:cspace:(.*):(.*):name\\((.*)\\)\\'?([^\\']*)\\'?";
    public static final String AUTHORITY_ITEM_REGEX = "urn:cspace:(.*):(.*):name\\((.*)\\):item:name\\((.*)\\)\\'?([^\\']*)\\'?";
    public static final String AUTHORITY_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID)'displayName'";
    public static final String AUTHORITY_EXAMPLE2 = "urn:cspace:collectionspace.org:Loansin:name(shortID)";
    public static final String AUTHORITY_ITEM_EXAMPLE = "urn:cspace:collectionspace.org:Loansin:name(shortID):item:name(itemShortID)'itemDisplayName'";
    public static final String EX_tenantName = "collectionspace.org";
    public static final String EX_resource = "Loansin";
    public static final String EX_shortIdentifier = "shortID";
    public static final String EX_displayName = "displayName";
    public static final String EX_itemShortIdentifier = "itemShortID";
    public static final String EX_itemDisplayName = "itemDisplayName";

    public static class Authority {

        public String tenantName = "";
        public String resource = "";
        public String shortIdentifier = "";
        public String displayName = "";

        public static Authority parse(String urn) {
            Authority info = new Authority();
            Pattern p = Pattern.compile(AUTHORITY_REGEX);
            Matcher m = p.matcher(urn);
            if (m.find()) {
                if (m.groupCount() < 4) {
                    return null;
                }
                info.tenantName = m.group(1);
                info.resource = m.group(2);
                info.shortIdentifier = m.group(3);
                info.displayName = m.group(4);
                return info;
            }
            return null;
        }
        
        public String getShortIdentifier() {
            return this.shortIdentifier;
        }

        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other instanceof Authority) {
                Authority ao = (Authority) other;
                return (this.tenantName.equals(ao.tenantName)
                        && this.resource.equals(ao.resource)
                        && this.shortIdentifier.equals(ao.shortIdentifier));
            } else {
                return false;
            }
        }

        public String getRelativeUri() {
            return "/" + resource + "/" + URN_NAME_PREFIX + "(" + shortIdentifier + ")";
        }

        public String toString() {
            String displaySuffix = (displayName != null && (!displayName.isEmpty())) ? '\'' + displayName + '\'' : "";
            return URN_PREFIX + tenantName + ':' + resource + ":" + "name" + "(" + shortIdentifier + ")" + displaySuffix;
        }
    }

    public static class AuthorityItem {

        public Authority inAuthority;
        public String shortIdentifier = "";
        public String displayName = "";

        public static AuthorityItem parse(String urn) {
            Authority info = new Authority();
            AuthorityItem termInfo = new AuthorityItem();
            termInfo.inAuthority = info;
            Pattern p = Pattern.compile(AUTHORITY_ITEM_REGEX);
            Matcher m = p.matcher(urn);
            if (m.find()) {
                if (m.groupCount() < 5) {
                	if (m.groupCount() == 4 && logger.isDebugEnabled()) {
                		logger.debug("AuthorityItem.parse only found 4 items; Missing displayName? Urn:"+urn);
                	}
                    return null;
                }
                termInfo.inAuthority.tenantName = m.group(1);
                termInfo.inAuthority.resource = m.group(2);
                termInfo.inAuthority.shortIdentifier = m.group(3);
                termInfo.shortIdentifier = m.group(4);
                termInfo.displayName = m.group(5);
                return termInfo;
            }
            return null;
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

    public static Authority buildAuthority(String tenantName, String serviceName, String authorityShortIdentifier, String authorityDisplayName) {
        Authority authority = new Authority();
        authority.tenantName = tenantName;
        authority.resource = serviceName;
        if (Tools.notEmpty(authority.resource)) {
            authority.resource = authority.resource.toLowerCase();
        }
        authority.shortIdentifier = authorityShortIdentifier;
        authority.displayName = authorityDisplayName;
        return authority;
    }

    public static AuthorityItem buildAuthorityItem(String tenantName, String serviceName, String authorityShortIdentifier,
            String itemShortIdentifier, String itemDisplayName) {
        Authority authority = buildAuthority(tenantName, serviceName, authorityShortIdentifier, "");
        return buildAuthorityItem(authority, itemShortIdentifier, itemDisplayName);
    }

    public static AuthorityItem buildAuthorityItem(String authorityRefName, String itemShortID, String itemDisplayName) {
        Authority authority = Authority.parse(authorityRefName);
        AuthorityItem item = buildAuthorityItem(authority, itemShortID, itemDisplayName);
        return item;
    }

    public static AuthorityItem buildAuthorityItem(Authority authority, String itemShortIdentifier, String itemDisplayName) {
        AuthorityItem item = new AuthorityItem();
        item.inAuthority = authority;
        item.shortIdentifier = itemShortIdentifier;
        item.displayName = itemDisplayName;
        return item;
    }

    /** Use this method to avoid formatting any urn's outside of this unit;
     * Caller passes in a shortId, such as "TestAuthority", and method returns
     * the correct urn path element, without any path delimiters such as '/'
     * so that calling shortIdToPath("TestAuthority") returns "urn:cspace:name(TestAuthority)", and
     * then this value may be put into a path, such as "/personauthorities/urn:cspace:name(TestAuthority)/items".
     */
    public static String shortIdToPath(String shortId) {
        return URN_NAME_PREFIX + '(' + shortId + ')';
    }
}
