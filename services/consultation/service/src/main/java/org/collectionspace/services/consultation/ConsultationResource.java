/*
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:
 *
 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org
 *
 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.
 *
 *  You may obtain a copy of the ECL 2.0 License at
 *
 *  https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.consultation;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.collectionspace.services.client.ConsultationClient;
import org.collectionspace.services.common.NuxeoBasedResource;

@Path(ConsultationClient.SERVICE_PATH)
@Consumes("application/xml")
@Produces("application/xml")
public class ConsultationResource extends NuxeoBasedResource {

    @Override
    protected String getVersionString() {
        return "$LastChangedRevision$";
    }

    @Override
    public String getServiceName() {
        return ConsultationClient.SERVICE_NAME;
    }

    @Override
    public Class<ConsultationsCommon> getCommonPartClass() {
        return ConsultationsCommon.class;
    }
}
