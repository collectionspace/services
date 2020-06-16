package org.collectionspace.services.batch.nuxeo;

import java.util.Arrays;
import java.util.HashMap;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.common.invocable.InvocationContext.ListCSIDs;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.common.NuxeoBasedResource;

import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayloadOut;

import org.dom4j.DocumentException;
import org.dom4j.Element;

/**
 * A batch job that updates the following fields:
 * collectionobject_common: numberOfObjects, numberValue, material, fieldCollectionPlace, responsibleDepartment, assocPeople, numberType, objectProductionPerson, objectProductionPlace, fieldCollector, objectStatus, contentPlace, objectName
 * collectionobject_naturalistory: taxon
 * collectionobject_pahma: pahmaEthnographicFileCodeList, pahmaFieldLocVerbatim, inventoryCount
 * The list contexts is
 *
 *
 * The following parameters are allowed:
 *
 * targetCSID: csid of target records, a dictionary of parameters (fields to update) and their new values 
 *
 * @author Cesar Villalobos
 */

public class BulkObjectEditBatchJob extends  AbstractBatchJob {
  final Logger logger = LoggerFactory.getLogger(BulkObjectEditBatchJob.class);

  public BulkObjectEditBatchJob() {
    setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_LIST));
  }

  @Override
  public void run() {
    setCompletionStatus(STATUS_MIN_PROGRESS);
    try {
      InvocationContext ctx = getInvocationContext();

      String mode = ctx.getMode();


      if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
        ArrayList<String> csids  = new ArrayList<String>();
        csids.addAll(ctx.getListCSIDs().getCsid());
        HashMap<String, String>  fieldsToValues = this.getValues();
        InvocationResults results = new InvocationResults();

        if (fieldsToValues.isEmpty()) {
          throw new Exception("There is nothing to update. Aborting...");
        }

        int numAffected = 0 ;
        String payload = preparePayload(fieldsToValues);

        for (String csid : csids) {
          String mergedPayload = mergePayloads(csid, new PoxPayloadOut(payload.getBytes()));

          if (mergedPayload != null) {
            if(updateRecord(csid, mergedPayload) != -1) {
              numAffected += 1;
            } else {
              // Make this more obvious?
              logger.warn("The record with csid " +  csid + " was not updated.");
            }
          }
        }
        setCompletionStatus(STATUS_COMPLETE);
        results.setNumAffected(numAffected);
        results.setUserNote("Updated " + numAffected + " records with the following " + fieldsToValues.toString());

        setResults(results);
      } else {
        throw new Exception("Unsupported invocation mode " + mode);
      }
    } catch (Exception e) {
      setCompletionStatus(STATUS_ERROR);
      setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
    }
  }
  public String preparePayload(HashMap<String, String> fieldsToUpdate)  {
    String commonValues = "";

    String otherNumber = "<otherNumberList><otherNumber>";
    Boolean otherNumFlag = false;

    for (String key : fieldsToUpdate.keySet()) {
      String value = fieldsToUpdate.get(key);
      
      if (key.equals("material")) {
        commonValues += "<materialGroupList><materialGroup><" + key + ">" + value + "</" + key + "></materialGroup></materialGroupList>";
      } else if (key.equals("responsibleDepartment")) {
        commonValues += "<responsibleDepartments><" + key + ">" + value + "</" + key + "></responsibleDepartments>";
      } else if (key.equals("assocPeople")) {
        commonValues += "<assocPeopleGroupList><assocPeopleGroup><" + key + ">" + value + "</" + key + "></assocPeopleGroup></assocPeopleGroupList>";
      } else if (key.equals("objectProductionPerson")) {
        commonValues += "<objectProductionPersonGroupList><objectProductionPersonGroup><" + key + ">" + value + "</" + key + "></objectProductionPersonGroup></objectProductionPersonGroupList>";
      } else if (key.equals("objectProductionPlace")) {
        commonValues += "<objectProductionPlaceGroupList><objectProductionPlaceGroup><" + key + ">" + value + "</" + key + "></objectProductionPlaceGroup></objectProductionPlaceGroupList>";
      } else if (key.equals("fieldCollector")) {
        commonValues += "<fieldCollectors><" + key + ">" + value + "</" + key + "></fieldCollectors>";
      } else if (key.equals("objectStatus")) {
        commonValues += "<objectStatusList><" + key + ">" + value + "</" + key + "></objectStatusList>";
      } else if (key.equals("contentPlace")) {
        commonValues += "<contentPlaces><" + key + ">" + value + "</" + key + "></contentPlaces>";
      } else if (key.equals("objectName")) {
        commonValues += "<objectNameList><objectNameGroup><" + key + ">" + value + "</" + key + "></objectNameGroup></objectNameList>";
      } else if (key.equals("briefDescription")) {
        commonValues += "<briefDescriptions><" + key + ">" + value + "</" + key + "></briefDescriptions>";
      } else if (key.equals("numberValue") || key.equals("numberType")) {
        otherNumber += "<" + key + ">" + value + "</" + key + ">";
        otherNumFlag = true;
      } else if (key.equals("objectProductionDate")) {
        commonValues += "<objectProductionDateGroupList><objectProductionDateGroup><dateDisplayDate>" + value + "</dateDisplayDate></objectProductionDateGroup></objectProductionDateGroupList>";
      } else if (key.equals("contentDate")) {
        commonValues += "<contentDateGroup><dateDisplayDate>" + value + "</dateDisplayDate></contentDateGroup>";
      } else if (key.equals("fieldCollectionDateGroup")) {
          commonValues += "<fieldCollectionDateGroup><dateDisplayDate>" + value + "</dateDisplayDate></fieldCollectionDateGroup>";
      } else {
        commonValues += "<" + key + ">" + value + "</" + key + ">";
      }
    }

    if (otherNumFlag) {
      otherNumber += "</otherNumber></otherNumberList>";
      commonValues += otherNumber;
    }

    String commonPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<document name=\"collectionobjects\">" +
      "<ns2:collectionobjects_common " +
      "xmlns:ns2=\"http://collectionspace.org/services/collectionobject\" " +
      "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
      commonValues +
      "</ns2:collectionobjects_common></document>";

    return commonPayload;
  }

  public String mergePayloads(String csid, PoxPayloadOut batchPayload) throws Exception {
    // now we have the bytes for both t
    HashMap<String, Element> batchElementList = new HashMap<String, Element>();
    PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(csid);

    // Now we have a list of Elements that we can go thru and update
    for (PayloadOutputPart batchCandidatePart : batchPayload.getParts()) {
      Element batchCandidatePartElement = batchCandidatePart.asElement();
      batchElementList.put(batchCandidatePartElement.getName(), batchCandidatePartElement);
    }

    for (String batchPartElement : batchElementList.keySet()) {

      Element objectPartElement = collectionObjectPayload.getPart(batchPartElement).asElement();
      Element batchElement = batchElementList.get(batchPartElement);


      for (Element batchElementField : (List<Element>) batchElement.elements()) {
        String childElemName = batchElementField.getName();

        if (childElemName == null) {
          continue;
        }

        Element collectionObjElementList = objectPartElement.element(childElemName);
        
        if (collectionObjElementList != null) {
          for (Element objElem : (List<Element>) collectionObjElementList.elements()) {
            batchElementField.add(objElem.createCopy());
          }
          objectPartElement.remove(collectionObjElementList);
        }

        objectPartElement.add(batchElementField.createCopy());
      }
    }

    return collectionObjectPayload.asXML();
  }
  
  public int updateRecord(String csid, String payload) throws URISyntaxException, DocumentException {
    PoxPayloadOut collectionObjectPayload = findCollectionObjectByCsid(csid);

    int result = 0;

    try {
      ResourceMap resource = getResourceMap();
      NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resource.get(CollectionObjectClient.SERVICE_NAME);

      String response = new String(collectionObjectResource.update(getServiceContext(), resource, createUriInfo(), csid, payload));
    } catch (Exception e) {
      result = -1;
    }
    return result;

  }

  /*
   * @return a HashMap containing (K, V) pairs of  (Field, NewValue)
   */
  public HashMap<String, String>  getValues() {
    HashMap<String, String> results = new HashMap<String,  String>();
    for(Param param : this.getParams()) {
      if (param.getKey() != null) {
        String val = param.getValue();
        if (val != null && !val.equals("")) {
          results.put(param.getKey(), param.getValue());
        }
      }
    }
    return results;
  }
}
