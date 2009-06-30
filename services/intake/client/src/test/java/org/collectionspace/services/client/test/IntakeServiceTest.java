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

import org.collectionspace.services.intake.Intake;
import org.collectionspace.services.intake.IntakeList;
import org.collectionspace.services.client.IntakeClient;

/**
 * A IntakeNuxeoServiceTest.
 * 
 * @version $Revision:$
 */
public class IntakeServiceTest {

    private IntakeClient intakeClient = IntakeClient.getInstance();
    private String updateId = null;
    private String deleteId = null;

    @Test
    public void createIntake() {
    	long identifier = this.createIdentifier();
    	
    	Intake intake = createIntake(identifier);
        ClientResponse<Response> res = intakeClient.createIntake(intake);
        Assert.assertEquals(res.getStatus(), Response.Status.CREATED.getStatusCode());
        
        //store updateId locally for "update" test
        if (updateId == null) {
        	updateId = extractId(res);
        } else {
        	deleteId = extractId(res);
        	System.out.println("Set deleteId: " + deleteId);
        }
    }

    @Test(dependsOnMethods = {"createIntake"})
    public void updateIntake() {
    	ClientResponse<Intake> res = intakeClient.getIntake(updateId);
        Intake intake = res.getEntity();
        verbose("Got Intake to update with ID: " + updateId,
        		intake, Intake.class);
        
        //intake.setCsid("updated-" + updateId);
        intake.setEntryNumber("updated-" + intake.getEntryNumber());
        intake.setEntryDate("updated-" + intake.getEntryDate());
        
        // make call to update service
        res = intakeClient.updateIntake(updateId, intake);
        
        // check the response
        Intake updatedIntake = res.getEntity();        
        Assert.assertEquals(updatedIntake.getEntryDate(), intake.getEntryDate());
        verbose("updateIntake: ", updatedIntake, Intake.class);
        
        return;
    }

    @Test(dependsOnMethods = {"createIntake"})
    public void createCollection() {
    	for (int i = 0; i < 3; i++) {
    		this.createIntake();
    	}
    }
    
    @Test(dependsOnMethods = {"createCollection"})
    public void getIntakeList() {
        //the resource method is expected to return at least an empty list
        IntakeList coList = intakeClient.getIntakeList().getEntity();
        List<IntakeList.IntakeListItem> coItemList = coList.getIntakeListItem();
        int i = 0;
        for(IntakeList.IntakeListItem pli : coItemList) {
            verbose("getIntakeList: list-item[" + i + "] csid=" + pli.getCsid());
            verbose("getIntakeList: list-item[" + i + "] objectNumber=" + pli.getEntryNumber());
            verbose("getIntakeList: list-item[" + i + "] URI=" + pli.getUri());
            i++;
        }
    }

    @Test(dependsOnMethods = {"createCollection"})
    public void deleteIntake() {
    	System.out.println("Calling deleteIntake:" + deleteId);
        ClientResponse<Response> res = intakeClient.deleteIntake(deleteId);
        verbose("deleteIntake: csid=" + deleteId);
        verbose("deleteIntake: status = " + res.getStatus());
        Assert.assertEquals(res.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    private Intake createIntake(long identifier) {
    	Intake intake = createIntake("objectNumber-" + identifier,
    			"objectName-" + identifier);    	

        return intake;
    }

    private Intake createIntake(String entryNumber, String entryDate) {
    	Intake intake = new Intake();
    	
    	intake.setEntryNumber(entryNumber);
    	intake.setEntryDate(entryDate);

        return intake;
    }

    private String extractId(ClientResponse<Response> res) {
        MultivaluedMap mvm = res.getMetadata();
        String uri = (String) ((ArrayList) mvm.get("Location")).get(0);
        String[] segments = uri.split("/");
        String id = segments[segments.length - 1];
        verbose("id=" + id);
        return id;
    }

    private void verbose(String msg) {
        System.out.println("Intake Test: " + msg);
    }

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

    private void verboseMap(MultivaluedMap map) {
        for(Object entry : map.entrySet()){
            MultivaluedMap.Entry mentry = (MultivaluedMap.Entry) entry;
            verbose("    name=" + mentry.getKey() + " value=" + mentry.getValue());
        }
    }
    
    private long createIdentifier() {
    	long identifier = System.currentTimeMillis();
    	return identifier;
    }
}
