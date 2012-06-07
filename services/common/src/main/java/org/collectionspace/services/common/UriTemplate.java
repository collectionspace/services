/**
 * This document is a part of the source code and related artifacts for
 * CollectionSpace, an open source collections management system for museums and
 * related institutions:
 *
 * http://www.collectionspace.org http://wiki.collectionspace.org
 *
 * Copyright 2009-2012 University of California, Berkeley
 *
 * Licensed under the Educational Community License (ECL), Version 2.0. You may
 * not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 *
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.collectionspace.services.common;

import java.net.URI;
import java.util.Map;
import javax.ws.rs.core.UriBuilder;
import org.collectionspace.services.common.api.Tools;

public class UriTemplate {

    UriBuilder builder;
    String path;

    public UriTemplate(String path) {
        setUriPath(path);
        setBuilder();
    }

    private void setBuilder() {
        if (builder == null) {
            try {
                builder = UriBuilder.fromPath(path);
            } catch (IllegalArgumentException iae) {
                // FIXME: Need to add logger and log error
                // Will silently fail to initialize builder if relative URI is null
                // No other checking of path format is apparently done
            }
        }
    }

    private void setUriPath(String path) {
        if (Tools.notBlank(this.path)) {
            this.path = this.path;
        }
    }

    private UriBuilder getBuilder() {
        if (builder == null) {
            setBuilder();
        }
        return builder;
    }

    public String buildUri(Map<String, String> varsMap) {
        URI uri = getBuilder().buildFromMap(varsMap);
        if (uri != null) {
            return uri.toString();
        } else {
            return "";
        }
    }

}