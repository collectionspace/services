package org.collectionspace.services.client.test;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.Assert;
import org.testng.annotations.Test;

import org.collectionspace.services.relation.Relation;
import org.collectionspace.services.relation.RelationList;
import org.collectionspace.services.relation.RelationshipType;

import org.collectionspace.services.client.RelationClient;
import org.collectionspace.services.RelationJAXBSchema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A RelationNuxeoServiceTest.
 * 
 * @version $Revision:$
 */
public class RelationServiceTest {

    /** The relation client. */
    private RelationClient relationClient = RelationClient.getInstance();
    
    /** The update id. */
    private String updateId = null;
    
    /** The delete id. */
    private String deleteId = null;
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(RelationServiceTest.class);

    /**
     * Creates the relation.
     */
    @Test
    public void createRelation() {
        long identifier = this.createIdentifier();

        Relation relation = createRelation(identifier);
        ClientResponse<Response> res = relationClient.createRelation(relation);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        
        String responseString = res.toString();
        System.out.println(responseString);

        //store updateId locally for "update" test
        if(updateId == null){
            updateId = extractId(res);
        }else{
            deleteId = extractId(res);
            verbose("Set deleteId: " + deleteId);
        }
    }

    /**
     * Update relation.
     */
    @Test(dependsOnMethods = {"createRelation"})
    public void updateRelation() {
        ClientResponse<Relation> res = relationClient.getRelation(updateId);
        Relation relation = res.getEntity();
        verbose("Got Relation to update with ID: " + updateId,
                relation, Relation.class);

        //relation.setCsid("updated-" + updateId);
        relation.setDocumentId1("updated-" + relation.getDocumentId1());
        relation.setDocumentType1("updated-" + relation.getDocumentType1());

        // make call to update service
        res = relationClient.updateRelation(updateId, relation);

        // check the response
        Relation updatedRelation = res.getEntity();
        Assert.assertEquals(updatedRelation.getDocumentId1(), relation.getDocumentId1());
        verbose("updateRelation: ", updatedRelation, Relation.class);

        return;
    }

    /**
     * Creates the collection.
     */
    @Test(dependsOnMethods = {"createRelation"})
    public void createCollection() {
        for(int i = 0; i < 3; i++){
            this.createRelation();
        }
    }

    /**
     * Gets the relation list.
     * 
     * @return the relation list
     */
    @Test(dependsOnMethods = {"createCollection"})
    public void getRelationList() {
        //the resource method is expected to return at least an empty list
        RelationList coList = relationClient.getRelationList().getEntity();
        List<RelationList.RelationListItem> coItemList = coList.getRelationListItem();
        int i = 0;
        for(RelationList.RelationListItem pli : coItemList){
            verbose("getRelationList: list-item[" + i + "] csid=" + pli.getCsid());
            verbose("getRelationList: list-item[" + i + "] URI=" + pli.getUri());
            i++;
            System.out.println();
        }
    }

    /**
     * Delete relation.
     */
    @Test(dependsOnMethods = {"createCollection"})
    public void deleteRelation() {
        verbose("Calling deleteRelation:" + deleteId);
        ClientResponse<Response> res = relationClient.deleteRelation(deleteId);
        verbose("deleteRelation: csid=" + deleteId);
        verbose("deleteRelation: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    /**
     * Creates the relation.
     * 
     * @param identifier the identifier
     * 
     * @return the relation
     */
    private Relation createRelation(long identifier) {
        Relation relation = createRelation("documentId1-" + identifier,
                "documentType1-" + identifier + "-type",
                "documentType1-" + identifier + "-type",
                "documentType1-" + identifier + "-type",
                RelationshipType.fromValue(
                		RelationJAXBSchema.ENUM_RELATIONSHIP_TYPE_ASSOC));

        return relation;
    }

    /**
     * Creates the relation.
     * 
     * @param documentId1 the document id1
     * @param documentType1 the document type1
     * @param documentId2 the document id2
     * @param documentType2 the document type2
     * @param rt the rt
     * 
     * @return the relation
     */
    private Relation createRelation(String documentId1, String documentType1,
    		String documentId2, String documentType2, RelationshipType rt) {
        Relation relation = new Relation();

        relation.setDocumentId1(documentId1);
        relation.setDocumentType1(documentType1);
        relation.setDocumentId2(documentId2);
        relation.setDocumentType2(documentType2);
        
        relation.setRelationshipType(rt);

        return relation;
    }

    /**
     * Extract id.
     * 
     * @param res the res
     * 
     * @return the string
     */
    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    /**
     * Verbose.
     * 
     * @param msg the msg
     */
    private void verbose(String msg) {
//        if(logger.isInfoEnabled()){
//            logger.debug(msg);
//        }
        System.out.println(msg);
    }

    /**
     * Verbose.
     * 
     * @param msg the msg
     * @param o the o
     * @param clazz the clazz
     */
    private void verbose(String msg, Object o, Class clazz) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(clazz);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                    Boolean.TRUE);
            m.marshal(o, System.out);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Verbose map.
     * 
     * @param map the map
     */
    private void verboseMap(MultivaluedMap map) {
        for(Object entry : map.entrySet()){
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }

    /**
     * Creates the identifier.
     * 
     * @return the long
     */
    private long createIdentifier() {
        long identifier = System.currentTimeMillis();
        return identifier;
    }
}
