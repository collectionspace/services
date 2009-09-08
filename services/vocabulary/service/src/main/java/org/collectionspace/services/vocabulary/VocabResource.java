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
package org.collectionspace.services.vocabulary;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.collectionspace.services.common.vocabulary.VocabManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/vocab")
@Consumes("application/xml")
@Produces("application/xml")
public class VocabResource {

    public final static String SERVICE_NAME = "vocab";
    final Logger logger = LoggerFactory.getLogger(VocabResource.class);

    public VocabResource() {
        // do nothing
    }

    @GET
    @Path("{csid}")
    public void getVocab(
            @PathParam("csid") String csid) {
        if(logger.isDebugEnabled()){
            verbose("getVocab with csid=" + csid);
        }
        if(csid == null || "".equals(csid)){
            logger.error("getVocab: missing csid!");
            Response response = Response.status(Response.Status.BAD_REQUEST).entity(
                    "get failed on getVocab csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        try {
        	//
        	// An example call to the Vocabulary manager
        	//
        	VocabManager.exampleMethod("someParam");
        } catch(Exception e){
            if(logger.isDebugEnabled()){
                logger.debug("getVocab", e);
            }
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed on vocab csid=" + csid).type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }

        // an example of how to send a failed message
        if(false){
            Response response = Response.status(Response.Status.NOT_FOUND).entity(
                    "Get failed, the requested CSID:" + csid + ": was not found.").type(
                    "text/plain").build();
            throw new WebApplicationException(response);
        }
    }

    private void verbose(String msg) {
        System.out.println("VocabResource. " + msg);
    }
}
