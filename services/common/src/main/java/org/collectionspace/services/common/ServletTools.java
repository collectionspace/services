package org.collectionspace.services.common;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ServletTools {

    public final static int BROWSER_NOT_SET             = -1;
    public final static int BROWSER_UNKNOWN             = 0;
    public final static int BROWSER_IE4                 = 1;
    public final static int BROWSER_NETSCAPE_COMPATIBLE = 20;
    public final static int BROWSER_NETSCAPE_4          = 21;
    public final static int BROWSER_NETSCAPE_5          = 22;
    public final static int BROWSER_NETSCAPE_6          = 23;
    public final static int BROWSER_IE                  = 24;
    public final static int BROWSER_DYNAMIDE_TEXT       = 50;

    public final static String BROWSER_STRING_UNKNOWN = "*";
    public final static String BROWSER_STRING_IE = "IE";
    public final static String BROWSER_STRING_NETSCAPE_COMPATIBLE = "NS";

    public final static String getBrowserStringFromID(int id){
        switch ( id ) {
            case BROWSER_UNKNOWN : return BROWSER_STRING_UNKNOWN;
            case BROWSER_IE : return BROWSER_STRING_IE;
            case BROWSER_NETSCAPE_COMPATIBLE : return BROWSER_STRING_NETSCAPE_COMPATIBLE;
            default: return BROWSER_STRING_UNKNOWN;
        }
    }

    /** Different from ServletRequest.getParameter in that this function will never return a null, always and empty string if param not found.
     */
    public static String getParameterValue(HttpServletRequest request, String paramName){
        if (request == null){
            return "";
        }
        String value = request.getParameter(paramName);
        value = (value == null) ? "" : value;
        return value;
    }

    public static String getURL(HttpServletRequest request){
         if (request == null){
            return "";
         }
         String qs = request.getQueryString();
         String qstr = (qs != null && qs.length() > 0)
                ? "?"+qs
                : "";
         return request.getRequestURI()+qstr;
    }

    public static String getFullURL(HttpServletRequest request){
         return getProtoHostPort(request)+getURL(request);
    }

    /** @return "http" or "https", without the "://" part.
     */
    public static String getProto(HttpServletRequest request){
         if (request==null){
            return "";
         }
         String prot = request.getAuthType();
         String protocol = prot != null && prot.equals("SSL") ? "https" : "http";
         return protocol;
    }

    public static String getProtoHostPort(HttpServletRequest request){
         if (request==null){
            return "";
         }
         String protocol = getProto(request);
         int port = request.getServerPort();
         String portstr;
         if ( protocol.equals("https") ) {
            portstr = (port != 443) ? ":"+port : "";
         } else {
            portstr =  (port != 80)  ? ":"+port : "";
         }
         return protocol+"://"+request.getServerName()+portstr;
    }

    public static String getProtoHostPort(java.net.URL url){
         if (url==null){
            return "";
         }
         String protocol = url.getProtocol();
         int port = url.getPort();
         String portstr;
         if ( protocol.equals("https") ) {
            portstr = (port != 443) ? ":"+port : "";
         } else {
            portstr =  (port != 80)  ? ":"+port : "";
         }
         return protocol+"://"+url.getHost()+portstr;
    }

    public static String decodeURLString(HttpServletRequest request, String paramName) throws UnsupportedEncodingException{
        if ( request == null ) {
            return "";
        }
        String value = request.getParameter(paramName);
        return decodeURLString(value);
    }

    public static String decodeURLString(String URLString) throws UnsupportedEncodingException{
        if ( URLString == null ) {
            return "";
        }
        return URLDecoder.decode(URLString, "UTF-8");
    }

    public static String encodeURLString(String s){
        return URLEncoder.encode(s);
    }

    public static String dumpRequestInfo(HttpServletRequest request){
        return dumpRequestInfo(request, true, "#FFAD00", true);
    }

    public static String dumpRequestInfo(HttpServletRequest request, boolean html, String headerColor, boolean dumpHeaders){
        if (request==null){
            return "NULL REQUEST";
        }
        String result;
        if (dumpHeaders){
            result = dumpRequestHeaders(request, html);
        } else {
            result = "URL: " +  getFullURL(request);
        }

        if ( html ) {
           result = result + "\r\n<br />Params: ";
        } else {
           result = result + "\r\nParams: ";
        }
        result = result + dumpRequestParams(request, html, headerColor);
        return result;
    }

    public static String dumpRequestHeaders(HttpServletRequest request, boolean html){
        if (request==null){
            return "NULL REQUEST";
        }
        String headers = "";
        String nl = html ? "\r\n<br />" : "\r\n";
        for(Enumeration headernames = request.getHeaderNames(); headernames.hasMoreElements();){
            String headername =  (String)headernames.nextElement();
            headers += nl + headername+": "+request.getHeader(headername);
        }
        String result;
        if ( html ) {
            result = "<pre>";
        } else {
            result = "";
        }
        result = result + "\r\nHeaders: "+ headers
          +"\r\nmethod: " + request.getMethod()
          +"\r\nProtocol: " + getProto(request)
          +"\r\nURL: " +  getFullURL(request);
          //+"\r\nQuery String: " +  getQueryString()
          //+"\r\nContent: " + getContent();
          if ( html ) {
            result = result + "</pre>";
          }
        return result;
    }

    public static String dumpRequestParams(HttpServletRequest request){
        return dumpRequestParams(request, true, "#FFAD00");
    }
    public static String dumpRequestParams(HttpServletRequest request, boolean html, String headerColor){
        if (request==null){
            return "NULL REQUEST";
        }
        StringBuffer result = new StringBuffer();
        if (html) result.append( "<TABLE BORDER='1' cellpadding='0' cellspacing='0'>\n" +
                "<TR BGCOLOR='"+headerColor+"'>\n" +
                "<TH>Parameter Name</TH><TH>Parameter Value(s)</TH></TR>");
        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()) {
            String paramName = (String)paramNames.nextElement();

            if (html) result.append("\r\n<TR><TD>" + paramName + "\r\n</TD><TD>");
            else      result.append(paramName).append("=");

            String[] paramValues = request.getParameterValues(paramName);
            if (paramValues.length == 1) {
                String paramValue = paramValues[0];
                if (paramValue.length() == 0){
                    if (html) result.append("<I>No Value</I>");
                    else result.append("\"\"");
                } else {
                    if (html) result.append(paramValue);
                    else      result.append('\"'+paramValue+'\"');
                }
            } else {
                if (html) result.append("<UL>");
                for(int i=0; i<paramValues.length; i++) {
                    if (html) result.append("<LI>" + paramValues[i]+"</LI>");
                    else      result.append('\"'+paramValues[i]+'\"');
                }
                if (html) result.append("</UL>");
            }
            if (html) result.append("</TD>\r\n</TR>");
            else      result.append("\r\n");
        }
        if (html) result.append("</TABLE>");
        return result.toString();
    }



    public static class UserIDPassword {
        public String user_id = "";
        public String password = "";
    }

    /*

    public String getUserName(){
        String remoteUserName = getRemoteUser().user_id;
        if (remoteUserName.length() > 0){
            return remoteUserName;
        } else {
            String name = getFieldValue("USER");
            if ( name != null && name.length()>0 ) {
                return name;
            }
        }
        return "";
     }
    */
    public static UserIDPassword getRemoteUser(HttpServletRequest request){
        return getRemoteUser(request.getHeader("Authorization"));
    }

    protected static UserIDPassword getRemoteUser(String authString){
        UserIDPassword uip = new UserIDPassword();
        try {   // Decode and decompose the Authorization headervalue
                if (authString == null){
                    return uip;
                }
                authString = authString.substring(6).trim();
                byte mydata[];
//                sun.misc.BASE64Decoder base64 = new sun.misc.BASE64Decoder();
                org.apache.commons.codec.binary.Base64 base64 = new org.apache.commons.codec.binary.Base64();
//                mydata = base64.decodeBuffer(authString);
                mydata = base64.decode(authString);
                String loginInfo = new String(mydata);
                int index = loginInfo.indexOf(":");
                if( index != -1 ){
                    uip.password = loginInfo.substring(index +1);
                    uip.user_id = loginInfo.substring(0, index);
                }
        } catch(Exception e) {
            //result will have empty user name
            System.out.println("ServletTools.getRemoteUser() failed to obtain Authorization info");
        }
        return uip;
    }

    public static String browserVersion(HttpServletRequest request){
        String agent = request.getHeader("User-Agent");
        //Examples:
        //curl/7.5.1
        //Mozilla/4.0
        //Mozilla/4.08 [en] (Win95; U ;Nav)
        //Mozilla/4.0 (compatible; MSIE 5.01; Windows NT; FMRCo cfg. 5.01.2.1a)
        //Mozilla/4.0 (compatible; MSIE 5.01; Windows NT; FMRCo cfg. 5.01.2.1a)
		if ( agent == null ) {
			return "null";//don't just return "" since this value may be used in a Tcl eval, and it would then disappear.
		}
        int start, stop;
        if ( agent.indexOf("MSIE ") > -1 ) {
            start = agent.indexOf("MSIE ");
            stop = agent.indexOf(";", start);
            stop = stop > -1 ? stop : agent.length(); //safety
            return agent.substring(start+5, stop).trim();
        } else {
            start = agent.indexOf("/");
            stop = agent.indexOf(" ");
            stop = stop > -1 ? stop : agent.length(); //for JSSE, there is no space: User-Agent: Java1.3.0_02
            return agent.substring(start+1, stop).trim();
        }
    }


    /**
     * Mozilla/4.79 [en] (Windows NT 5.0; U)
     * Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:1.4b) Gecko/20030516 Mozilla Firebird/0.6
     * Mozilla/5.0 (Windows; U; Windows NT 5.0; en-US; rv:0.9.4) Gecko/20011128 Netscape6/6.2.1
     * Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0; .NET CLR 1.0.3705)
     */
    public static int findBrowserID(HttpServletRequest request){
        String browserString = request.getHeader("User-Agent");
        if (browserString == null)
            return BROWSER_UNKNOWN;
        if (browserString.indexOf("MSIE") > -1)
            return BROWSER_IE4;
        int iMozilla = browserString.indexOf("Mozilla");
        if (iMozilla > -1){
            String ver = browserString.substring(iMozilla + ("Mozilla/".length()) );
            int iSpace = ver.indexOf(" ");
            if (iSpace>-1){
                ver = ver.substring(0, iSpace);
            }
            try {
                Double majorminor =  new Double(ver);
                if (majorminor.longValue()==4){
                    return BROWSER_NETSCAPE_4;
                }
                if (majorminor.longValue()==5){
                    return BROWSER_NETSCAPE_5;
                }
                if (majorminor.longValue()==6){
                    return BROWSER_NETSCAPE_6;
                }
            } catch (Exception e)  {
                System.out.println("ERROR: silent after catching error converting browser minor version."+browserString);
                return BROWSER_NETSCAPE_COMPATIBLE;
            }
            return BROWSER_NETSCAPE_COMPATIBLE;
        }
        return BROWSER_UNKNOWN;
    }

    public static boolean isBrowserIE(int bid){
      return (bid == BROWSER_IE4 || bid == BROWSER_IE)  ? true : false ;
    }

    public static boolean isBrowserNS4x(int bid){
      return (bid == BROWSER_NETSCAPE_4) ;
    }

    public static String getCookieValue(HttpServletRequest request, String name ){
        Cookie result = findCookie(request, name);
        if ( result != null ) {
            return result.getValue();
        }
        return "";
    }

    public static Cookie findCookie(HttpServletRequest request, String name ){
        if (request == null || name == null) {
            return null;
        }
        Cookie [] cookies = request.getCookies();
        if ( cookies == null ) {
            return null;
        }
        int cookies_len = cookies.length;
        for (int i=0; i < cookies_len; i++) {
            Cookie cookie = cookies[i];
            if (cookie != null && name.equals(cookie.getName())){
                return cookie;
            }
        }
        return null;
    }

    public static Cookie setCookie(HttpServletResponse response, String name, String value){
        return setCookie(response, name, value, "/", 365*24*60*60);
    }

    public static Cookie setCookie(HttpServletResponse response, String name, String value, String path, int maxAge){
        Cookie cookie = new javax.servlet.http.Cookie(name, value);
        cookie.setMaxAge(maxAge);
        cookie.setPath(path);
        response.addCookie(cookie);
        return cookie;
    }



}

