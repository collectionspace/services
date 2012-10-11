package org.collectionspace.services.common.config;

// client module is built after this config module, so we can't currently include this
// import org.collectionspace.services.client.AuthorityClient;
import java.util.HashMap;
import java.util.Map;

// This class is a hack for CSPACE-5406

public class URIUtils {

    private static Map<String, String> docTypeSvcNameRegistry = new HashMap<String, String>();
    private static final String ITEMS_PATH_COMPONENT = "items";
    
    // FIXME: This is a quick hack, which assumes that URI construction
    // behaviors are bound to categories of services.  Those behaviors
    // should instead be specified on a per-service basis via a registry,
    // mechanism.  (See comments below for more details.) - ADR 2012-05-24
    public static final String AUTHORITY_SERVICE_CATEGORY = "authority";
    public static final String OBJECT_SERVICE_CATEGORY = "object";
    public static final String PROCEDURE_SERVICE_CATEGORY = "procedure";

    // FIXME: This is a quick hack; a stub / mock of a registry of
    // authority document types and their associated parent authority
    // service names. This MUST be replaced by a more general registry mechanism.
    // If not, the manually-entered values below represent a DRY violation,
    // and risk falling out of sync with comparable values elsewhere.
    // 
    // Per Patrick, this registry needs to be available system-wide;
    // extend to all relevant record types; and be automatically built
    // in some manner, such as via per-resource registration, from
    // existing configuration, etc. - ADR 2012-05-24
    private static Map<String, String> getDocTypeSvcNameRegistry() {
        if (docTypeSvcNameRegistry.isEmpty()) {
            docTypeSvcNameRegistry.put("Conceptitem", "Conceptauthorities");
            docTypeSvcNameRegistry.put("Locationitem", "Locationauthorities");
            docTypeSvcNameRegistry.put("Person", "Personauthorities");
            docTypeSvcNameRegistry.put("Placeitem", "Placeauthorities");
            docTypeSvcNameRegistry.put("Organization", "Orgauthorities");
            docTypeSvcNameRegistry.put("Taxon", "Taxonomyauthority");
        }
        return docTypeSvcNameRegistry;
    }

    /**
     * Return the parent authority service name, based on the item document
     * type.
     */
    public static String getAuthoritySvcName(String docType) {
        return getDocTypeSvcNameRegistry().get(docType);
    }

    // FIXME: The following URI construction methods are also intended to be
    // made generally available and associated to individual services, via the
    // registry mechanism described above. - ADR, 2012-05-24
    public static String getUri(String serviceName, String docID) {
        return "/" + serviceName.toLowerCase()
                + "/" + docID;
    }

    public static String getAuthorityItemUri(String authorityServiceName, String inAuthorityID, String docID) {
        return "/" + authorityServiceName.toLowerCase()
                + '/' + inAuthorityID
                + '/' + ITEMS_PATH_COMPONENT // AuthorityClient.ITEMS
                + '/' + docID;
    }
    
    // FIXME: Create equivalent getUri-type method(s) for sub-resources,
    // such as contacts - ADR, 2012-05-24
}
