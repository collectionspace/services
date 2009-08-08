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
import org.collectionspace.services.common.relation.RelationJAXBSchema;

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
    
    /** The logger. */
    final Logger logger = LoggerFactory.getLogger(RelationServiceTest.class);

    /**
     * Creates the relation.
     */
    @Test
    public void createRelation() {
        long identifier = this.createIdentifier();
        this.createRelationEntity(identifier);
    }    

    /**
     * Update relation.
     */
    @Test(dependsOnMethods = {"createRelation"})
    public void updateRelation() {
    	
    	String relationID = this.createRelationEntity(1);
    	Assert.assertNotNull(relationID, "Could not create a new object to update.");
    	
        ClientResponse<Relation> res = relationClient.getRelation(relationID);
        Relation theRelation = res.getEntity();
        verbose("Got Relation to update with ID: " + relationID,
        		theRelation, Relation.class);

        //relation.setCsid("updated-" + updateId);
        theRelation.setDocumentId1("updated-" + theRelation.getDocumentId1());
        theRelation.setDocumentType1("updated-" + theRelation.getDocumentType1());
        theRelation.setDocumentId2("updated-" + theRelation.getDocumentId2());
        theRelation.setDocumentType2("updated-" + theRelation.getDocumentType2());

        // make call to update service
        res = relationClient.updateRelation(relationID, theRelation);

        // check the response
        Relation updatedRelation = res.getEntity();
        Assert.assertEquals(updatedRelation.getDocumentId1(), theRelation.getDocumentId1());
        Assert.assertEquals(updatedRelation.getDocumentType1(), theRelation.getDocumentType1());
        Assert.assertEquals(updatedRelation.getDocumentId2(), theRelation.getDocumentId2());
        Assert.assertEquals(updatedRelation.getDocumentType2(), theRelation.getDocumentType2());
        
        verbose("updateRelation: ", updatedRelation, Relation.class);

        return;
    }

    /**
     * Creates a set of three relation objects.
     */
    @Test(dependsOnMethods = {"createRelation"})
    public void createRelationList() {
        for(int i = 0; i < 3; i++){
            this.createRelation();
        }
    }

    /**
     * Gets the relation list.
     * 
     * @return the relation list
     */
    @Test(dependsOnMethods = {"createRelationList"})
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
    @Test(dependsOnMethods = {"createRelation", "updateRelation"})
    public void deleteRelation() {
    	
    	String relationID = this.createRelationEntity(0);
    	Assert.assertNotNull(relationID, "Could not create a new object to delete.");
    	
        verbose("Calling deleteRelation:" + relationID);
        ClientResponse<Response> res = relationClient.deleteRelation(relationID);
        
        verbose("deleteRelation: csid=" + relationID);
        verbose("deleteRelation: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.OK.getStatusCode());
    }
    
    @Test(dependsOnMethods = {"createRelation"})
    public void relateObjects() {
    }

    /*
     * Private Methods
     */
    
    private String createRelationEntity(long identifier) {

        String result = null;
        
        Relation relation = new Relation();
        fillRelation(relation, identifier);
        ClientResponse<Response> res = relationClient.createRelation(relation);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        
        result = extractId(res);
        String responseString = res.toString();
        System.out.println(responseString);
        
        return result;
    }
    
    /**
     * Fills the relation.
     * 
     * @param identifier the identifier
     * 
     * @return the relation
     */
    private void fillRelation(Relation relation, long identifier) {
        fillRelation(relation, "Subject-" + identifier,
                "SubjectType-" + identifier + "-type",
                "Object-" + identifier,
                "ObjectType-" + identifier + "-type",
                RelationshipType.fromValue(
                		RelationJAXBSchema.ENUM_REL_TYPE_ASSOC));
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
    private void fillRelation(Relation relation, String documentId1, String documentType1,
    		String documentId2, String documentType2, RelationshipType rt)
    {
        relation.setDocumentId1(documentId1);
        relation.setDocumentType1(documentType1);
        relation.setDocumentId2(documentId2);
        relation.setDocumentType2(documentType2);
        
        relation.setRelationshipType(rt);
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
        System.out.println(msg);
    }

    /**
     * Verbose.
     * 
     * @param msg the msg
     * @param o the o
     * @param clazz the clazz
     */
    private void verbose(String msg, Object o, Class theClass) {
        try{
            verbose(msg);
            JAXBContext jc = JAXBContext.newInstance(theClass);
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
