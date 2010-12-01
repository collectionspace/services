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
 *  limitations under the License.
 */
package org.collectionspace.services.media;

import org.collectionspace.services.common.ResourceBase;
import org.collectionspace.services.common.ClientType;
import org.collectionspace.services.common.ServiceMain;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;

@Path("/media")
@Consumes("multipart/mixed")
@Produces("multipart/mixed")
public class MediaResource extends ResourceBase {

    @Override
    public String getServiceName(){
        return "media";
    };

    //FIXME retrieve client type from configuration
    final static ClientType CLIENT_TYPE = ServiceMain.getInstance().getClientType();

    @Override
    protected String getVersionString() {
    	final String lastChangeRevision = "$LastChangedRevision: 2108 $";
    	return lastChangeRevision;
    }

    @Override
    public Class<MediaCommon> getCommonPartClass() {
    	return MediaCommon.class;
    }

    public Class getResourceClass() {
        return this.getClass();
    }

    public MediaCommonList getMediaList(MultivaluedMap<String, String> queryParams) {
        return (MediaCommonList)getList(queryParams);
    }

    @Deprecated
    public MediaCommonList getMediaList(List<String> csidList) {
        return (MediaCommonList) getList(csidList);
    }

    protected MediaCommonList search(MultivaluedMap<String,String> queryParams,String keywords) {
         return (MediaCommonList) super.search(queryParams, keywords);
    }

    
}
