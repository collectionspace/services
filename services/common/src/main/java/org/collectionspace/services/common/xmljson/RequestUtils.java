package org.collectionspace.services.common.xmljson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.util.MediaTypeHelper;

/**
 * Utility methods for working with requests when doing
 * XML/JSON conversion.
 */
public class RequestUtils {
    
    /**
     * Determines if a request's content type is JSON.
     * 
     * @param request the request
     * @return true if the request contains JSON content, false otherwise
     */
    public static boolean isJsonContent(HttpServletRequest request) {
        return StringUtils.equals(request.getContentType(), MediaType.APPLICATION_JSON);
    }
    
    /**
     * Determines if a request's preferred response content
     * type is JSON.
     * 
     * @param request the request
     * @return true if JSON is preferred, false otherwise
     */
    public static boolean isJsonPreferred(HttpServletRequest request) {
        return isJsonPreferred(getAccept(request));
    }
    
    /**
     * Determines if an HTTP Accept header's preferred content
     * type is JSON.
     * 
     * @param accept the Accept header value
     * @return true if JSON is preferred, false otherwise
     */
    public static boolean isJsonPreferred(String accept) {
        if (StringUtils.isBlank(accept)) {
            return false;
        }
        
        List<MediaType> mediaTypes = MediaTypeHelper.parseHeader(accept);
        
        if (mediaTypes.size() == 0) {
            return false;
        }
        
        MediaTypeHelper.sortByWeight(mediaTypes);

        return MediaTypeHelper.equivalent(mediaTypes.get(0), MediaType.APPLICATION_JSON_TYPE);
    }
    
    /**
     * Constructs an Accept header value from the Accept header
     * value in the given request, ensuring that XML will be
     * accepted. If the request already accepts XML, the Accept
     * value is returned unchanged. If the request does not
     * accept XML, XML is appended to the Accept value, with the
     * lowest possible quality factor.
     * 
     * @param request the request
     * @return the possibly modified header value, which is
     *         assured to accept XML
     */
    public static String getXmlEnsuredAccept(HttpServletRequest request) {
        return getXmlEnsuredAccept(getAccept(request));
    }
    
    /**
     * Constructs an Accept header value from a base value,
     * ensuring that XML will be accepted. If the base value
     * already accepts XML, the base value is returned unchanged.
     * If the base value does not accept XML, XML is appended to
     * the value, with the lowest possible quality factor.
     * 
     * @param accept the base Accept header value
     * @return the possibly modified header value, which is
     *         assured to accept XML
     */
    public static String getXmlEnsuredAccept(String accept) {
        if (accept.contains(MediaType.APPLICATION_XML)) {
            return accept;
        }
        
        return (accept + "," + MediaType.APPLICATION_XML + ";q=0.001");
    }

    /**
     * Returns the value of the HTTP Accept header(s) for a given request.
     * If multiple Accept headers are present, their values are combined into
     * one value by joining with a comma.
     * 
     * @param request the request
     * @return the (possibly combined) value of the Accept header(s)
     */
    public static String getAccept(HttpServletRequest request) {
        List<?> headers = Collections.list(request.getHeaders(HttpHeaders.ACCEPT));
        List<String> strings = new ArrayList<String>(headers.size());
        
        for (Object header : headers) {
            strings.add(header.toString());
        }
        
        return StringUtils.join(strings, ",");
    }
}
