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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.noelios.restlet.util.Base64;

public class PortalSSOAuthenticationProvider {

    private static final String TOKEN_SEP = ":";

    private static final String TS_HEADER = "NX_TS";

    private static final String RANDOM_HEADER = "NX_RD";

    private static final String TOKEN_HEADER = "NX_TOKEN";

    private static final String USER_HEADER = "NX_USER";

    public static Map<String, String> getHeaders(String secretKey,
            String userName) {

        Map<String, String> headers = new HashMap<String, String>();

        Date timestamp = new Date();
        int randomData = new Random(timestamp.getTime()).nextInt();

        String clearToken = timestamp.getTime() + TOKEN_SEP + randomData
                + TOKEN_SEP + secretKey + TOKEN_SEP + userName;

        byte[] hashedToken;

        try {
            hashedToken = MessageDigest.getInstance("MD5").digest(
                    clearToken.getBytes());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }

        String base64HashedToken = Base64.encodeBytes(hashedToken);

        headers.put(TS_HEADER, String.valueOf(timestamp.getTime()));
        headers.put(RANDOM_HEADER, String.valueOf(randomData));
        headers.put(TOKEN_HEADER, base64HashedToken);
        headers.put(USER_HEADER, userName);

        return headers;
    }

}
