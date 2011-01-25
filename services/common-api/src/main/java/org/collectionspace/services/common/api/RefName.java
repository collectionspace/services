package org.collectionspace.services.common.api;

/**
 * User: laramie
 * $LastChangedRevision:  $
 * $LastChangedDate:  $
 */
public class RefName {

    public static final String URN_PREFIX = "urn:cspace:";

    public static String buildRefNameForAuthority(String tenantName,
                                          String serviceName,
                                          String authorityShortIdentifier,
                                          String authorityDisplayName
                                          ){
            return buildRefName(tenantName, serviceName, authorityShortIdentifier, authorityDisplayName, "");
        }

    /** Build a full refName given all the parameters; if you don't know the tenantName or the serviceName, but you do knw the authority's refName,
     * you can call buildRefNameForItemInAuthority(authorityRefBaseName, itemShortIdentifier, itemDisplayName) */
    public static String buildRefNameForItem(String tenantName, String serviceName, String authorityShortIdentifier, String itemShortIdentifier, String itemDisplayName){
        String authorityRefBaseName = buildRefName(tenantName, serviceName, authorityShortIdentifier, "", "");
        return buildRefName("", "", itemShortIdentifier, itemDisplayName, authorityRefBaseName);

    }

      /** Will build a refName string for you that is correct for an authority, or for an authority item, based on whether
     *  authorityRefBaseName is empty or not.  This is a general utility method.  If you want to just build a refName for an item,
     * use buildRefNameForItem() and if you want to build one for an authority, use buildRefNameForAuthority().   You would
     * use this method if you have the authority refName, and wish to build a refName for your item, without having to know
     * the tenantName or service name, you can call buildRefNameForItemInAuthority(authorityRefBaseName, itemShortIdentifier, itemDisplayName). */
    public static String buildRefName(String tenantName,
                                      String serviceName,
                                      String shortIdentifier,
                                      String displayName,
                                      String authorityRefBaseName){
        //CSPACE-3178
        String refname = "";
        String displaySuffix = (displayName!=null&&(!displayName.isEmpty())) ? '\''+displayName+'\'' : "";
        if (authorityRefBaseName!=null&&(!authorityRefBaseName.isEmpty())){
            //Then I have a parent, so emit short version of me after parent refName, stripped of displayName.
            String base = authorityRefBaseName;
            if (authorityRefBaseName.endsWith("'")){
                //strip off parent's displayName.  Could be done using AuthorityInfo and AuthorityTermInfo objects instead.
                base = base.substring(0, base.indexOf('\''));
            }
            refname = base+":items("+shortIdentifier+")"+displaySuffix;
        } else {
            //else I am the parent, just store my refName.
            refname = URN_PREFIX+tenantName+':'+serviceName+"("+shortIdentifier+")"+displaySuffix;
        }
        return refname;
    }
}
