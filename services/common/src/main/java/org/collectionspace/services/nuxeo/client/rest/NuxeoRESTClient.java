/*
 * (C) Copyright 2006-2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id: JOOoConvertPluginImpl.java 18651 2007-05-13 20:28:53Z sfermigier $
 */
package org.collectionspace.services.nuxeo.client.rest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;


import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.Series;

public class NuxeoRESTClient {

    public static final int AUTH_TYPE_NONE = 0;
    public static final int AUTH_TYPE_BASIC = 1;
    public static final int AUTH_TYPE_SECRET = 2;
    protected String baseURL = "http://127.0.0.1:8080/nuxeo";
    protected String restPrefix = "restAPI";
    protected String davPrefix = "dav";
    protected List<Cookie> cookies;
    protected int authType = AUTH_TYPE_NONE;
    protected String userName;
    protected String password;
    protected String secretToken;
    protected Client restClient;

    public NuxeoRESTClient(String baseURL) {
        this.baseURL = baseURL;
    }

    public NuxeoRESTClient(String protocol, String serverIP, String serverPort) {
        this(protocol, serverIP, serverPort, "nuxeo");
    }

    public NuxeoRESTClient(String protocol, String serverIP, String serverPort,
            String servletPath) {
        StringBuffer sb = new StringBuffer();
        sb.append(protocol);
        sb.append("://");
        sb.append(serverIP);
        if(serverPort != null && !serverIP.equals("80")){
            sb.append(':');
            sb.append(serverPort);
        }
        sb.append('/');
        sb.append(servletPath);
        this.baseURL = sb.toString();
    }

    public void setBasicAuthentication(String userName, String password) {
        authType = AUTH_TYPE_BASIC;
        this.userName = userName;
        this.password = password;
    }

    public void setSharedSecretAuthentication(String userName,
            String secretToken) {
        authType = AUTH_TYPE_SECRET;
        this.userName = userName;
        this.secretToken = secretToken;
    }

    public void setCookies(List<Cookie> cookies) {
        this.cookies = cookies;
    }

    public Representation post(List<String> pathParams,
            Map<String, String> queryParams, InputStream istream) {
        String path = "";
        StringBuffer pathBuffer = new StringBuffer();

        if(pathParams != null){
            for(String p : pathParams){
                pathBuffer.append(p);
                pathBuffer.append('/');
            }
            path = pathBuffer.toString();
        }

        return post(path, queryParams, istream);
    }

    public Representation post(String subPath,
            Map<String, String> queryParams, InputStream istream) {
        StringBuffer urlBuffer = new StringBuffer();

        if(subPath.startsWith("/")){
            subPath = subPath.substring(1);
        }
        if(subPath.endsWith("/")){
            subPath = subPath.substring(0, subPath.length() - 1);
        }

        urlBuffer.append(baseURL);
        urlBuffer.append('/');
        urlBuffer.append(restPrefix);
        urlBuffer.append('/');
        urlBuffer.append(subPath);

        if(queryParams != null){
            urlBuffer.append('?');

            String qpValue = null;
            for(String qpName : queryParams.keySet()){
                urlBuffer.append(qpName);
                urlBuffer.append('=');
                qpValue = queryParams.get(qpName);
                if(qpValue != null){
                    urlBuffer.append(qpValue.replaceAll(" ", "%20"));
                }
                urlBuffer.append('&');
            }
        }

        String completeURL = urlBuffer.toString();
        System.out.println("\nNuxeoRESTClient: calling " + completeURL);
        Request request = new Request(Method.POST, completeURL);

        setupAuth(request);
        setupCookies(request);
        final InputStream in = istream;
        request.setEntity(new OutputRepresentation(
                MediaType.MULTIPART_FORM_DATA) {

            @Override
            public void write(OutputStream outputStream) throws IOException {
                byte[] buffer = new byte[1024 * 64];
                int read;
                while((read = in.read(buffer)) != -1){
                    outputStream.write(buffer, 0, read);
                }

            }
        });

        return getRestClient().handle(request).getEntity();
    }

    public Representation get(List<String> pathParams,
            Map<String, String> queryParams) {
        String path = "";
        StringBuffer pathBuffer = new StringBuffer();

        if(pathParams != null){
            for(String p : pathParams){
                pathBuffer.append(p);
                pathBuffer.append('/');
            }
            path = pathBuffer.toString();
        }

        return get(path, queryParams);
    }

    public Representation get(String subPath,
            Map<String, String> queryParams) {
        StringBuffer urlBuffer = new StringBuffer();

        if(subPath.startsWith("/")){
            subPath = subPath.substring(1);
        }
        if(subPath.endsWith("/")){
            subPath = subPath.substring(0, subPath.length() - 1);
        }

        urlBuffer.append(baseURL);
        urlBuffer.append('/');
        urlBuffer.append(restPrefix);
        urlBuffer.append('/');
        urlBuffer.append(subPath);

        if(queryParams != null){
            urlBuffer.append('?');
            for(String qpName : queryParams.keySet()){
                urlBuffer.append(qpName);
                urlBuffer.append('=');
                urlBuffer.append(queryParams.get(qpName).replaceAll(" ", "%20"));
                urlBuffer.append('&');
            }
        }

        String completeURL = urlBuffer.toString();
        System.out.println("\nNuxeoRESTClient: calling " + completeURL);
        Request request = new Request(Method.GET, completeURL);
        setupAuth(request);
        setupCookies(request);

        return getRestClient().handle(request).getEntity();
    }

    protected void setupAuth(Request request) {

        if(authType == AUTH_TYPE_BASIC){
            ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
            ChallengeResponse authentication = new ChallengeResponse(scheme,
                    userName, password);
            request.setChallengeResponse(authentication);

        }else if(authType == AUTH_TYPE_SECRET){
            Series<Parameter> additionnalHeaders = new Form();

            Map<String, String> securityHeaders = PortalSSOAuthenticationProvider.getHeaders(
                    secretToken, userName);

            for(String hn : securityHeaders.keySet()){
                additionnalHeaders.add(hn, securityHeaders.get(hn));
            }

            request.getAttributes().put("org.restlet.http.headers",
                    additionnalHeaders);
        }
    }

    protected void setupCookies(Request request) {
        if(cookies != null){
            request.getCookies().clear();
            for(Cookie cookie : cookies){
                request.getCookies().add(cookie);
            }
        }

    }

    Client getRestClient() {
        if(restClient == null){
            if(baseURL.startsWith("https")){
                restClient = new Client(Protocol.HTTPS);
            }else{
                restClient = new Client(Protocol.HTTP);
            }
        }

        return restClient;
    }

    public int getAuthType() {
        return authType;
    }

    public void setAuthType(int authType) {
        this.authType = authType;
    }

    public String getDavPrefix() {
        return davPrefix;
    }

    public void setDavPrefix(String davPrefix) {
        this.davPrefix = davPrefix;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRestPrefix() {
        return restPrefix;
    }

    public void setRestPrefix(String restPrefix) {
        this.restPrefix = restPrefix;
    }

    public String getSecretToken() {
        return secretToken;
    }

    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * buildUrl build URL from given path and query parameters
     * @param pathParams
     * @param queryParams
     * @return
     */
    String buildUrl(List<String> pathParams,
            Map<String, String> queryParams) {
        String subPath = "";
        StringBuffer pathBuffer = new StringBuffer();

        if(pathParams != null){
            for(String p : pathParams){
                pathBuffer.append(p);
                pathBuffer.append('/');
            }
            subPath = pathBuffer.toString();
        }

        StringBuffer urlBuffer = new StringBuffer();

        if(subPath.startsWith("/")){
            subPath = subPath.substring(1);
        }
        if(subPath.endsWith("/")){
            subPath = subPath.substring(0, subPath.length() - 1);
        }

        urlBuffer.append(baseURL);
        urlBuffer.append('/');
        urlBuffer.append(restPrefix);
        urlBuffer.append('/');
        urlBuffer.append(subPath);

        if(queryParams != null){
            urlBuffer.append('?');

            String qpValue = null;
            for(String qpName : queryParams.keySet()){
                urlBuffer.append(qpName);
                urlBuffer.append('=');
                qpValue = queryParams.get(qpName);
                if(qpValue != null){
                    urlBuffer.append(qpValue.replaceAll(" ", "%20"));
                }
                urlBuffer.append('&');
            }
        }

        return urlBuffer.toString();
    }
}
