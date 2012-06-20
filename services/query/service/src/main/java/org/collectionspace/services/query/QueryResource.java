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
package org.collectionspace.services.query;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
//import javax.xml.bind.JAXBContext;
//import javax.xml.bind.Marshaller;

import org.collectionspace.services.common.query.QueryManager;
//import org.collectionspace.services.common.NuxeoClientType;
/*import org.collectionspace.services.common.ServiceMain;
import org.collectionspace.services.common.document.DocumentNotFoundException;
import org.collectionspace.services.common.document.DocumentHandler;
import org.collectionspace.services.common.repository.RepositoryClient;
import org.collectionspace.services.common.repository.RepositoryClientFactory;
import org.jboss.resteasy.util.HttpResponseCodes;
*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/query")
@Consumes("application/xml")
@Produces("application/xml")
public class QueryResource {

    public final static String SERVICE_NAME = "query";
    final Logger logger = LoggerFactory.getLogger(QueryResource.class);
    //FIXME retrieve client type from configuration
    //final static NuxeoClientType CLIENT_TYPE = ServiceMain.getInstance().getNuxeoClientType();

    public QueryResource() {
        // do nothing
    }

    @GET
    @Path("{csid}")
    public void getQuery(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getQuery with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getQuery: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on getQuery csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        try {
        	QueryManager.execQuery(csid);
        } catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getQuery", e);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on query csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        if(false){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

//        return intakeObject;
    }

//    private void verbose(String msg, Intake intakeObject) {
//        try{
//            verbose(msg);
//            JAXBContext jc = JAXBContext.newInstance(
//                    Intake.class);
//
//            Marshaller m = jc.createMarshaller();
//            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
//            m.marshal(intakeObject, System.out);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//
//    }

    private void verbose(String msg) {
        System.out.println("QueryResource. " + msg);
    }
}
