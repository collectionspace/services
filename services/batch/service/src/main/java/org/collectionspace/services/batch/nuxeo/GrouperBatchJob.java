package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.ws.rs.core.UriInfo;

import org.collectionspace.services.batch.BatchCommon;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.collectionobject.CollectionObjectResource;

import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.collectionspace.services.jaxb.AbstractCommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.group.nuxeo.GroupConstants;

/**
  This batch job creates a new group using the records passed in. Only group context is supported.
  @author Cesar Villalobos
*/

public class GrouperBatchJob extends AbstractBatchJob {
  final Logger logger = LoggerFactory.getLogger(GrouperBatchJob.class);

  public GrouperBatchJob() {
    setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_GROUP));
  }

  @Override
  public void run() {
	  run(null);
  }

  @Override
  public void run(BatchCommon batchCommon) {
    setCompletionStatus(STATUS_MIN_PROGRESS);

    String mode = getInvocationContext().getMode(); //FIXME: unused variable

    try { 

      boolean createNew = false; //FIXME: unused variable
      ArrayList<String> displayNames =  new ArrayList<String>();

      for (Param param : this.getParams()) {
        String key = param.getKey();

        if (key.equals("groupItems")) {
          displayNames.addAll(Arrays.asList(param.getValue().split(",")));
        }
      }
      String groupCSID = invocationCtx.getGroupCSID();

      ArrayList<String> listCsids = getObjectCSIDs(displayNames);
      logger.info("List of CSIDs: " + listCsids.toString());

      int numberCreated = 0;
      for (String csid : listCsids) {
        if (createRelation(groupCSID, GroupConstants.NUXEO_DOCTYPE, csid, CollectionObjectConstants.NUXEO_DOCTYPE, "affects") == null) {
          break;
        } else {
          numberCreated += 1;
        }
      }

      if (completionStatus != STATUS_ERROR) {
        results.setNumAffected(numberCreated);
        results.setUserNote("GrouperBatchJob updated group with csid " + groupCSID + " and linked " + numberCreated + " collection object records.");
        setCompletionStatus(STATUS_COMPLETE);
      }

    } catch (Exception e) {
      completionStatus = STATUS_ERROR;
			errorInfo = new InvocationError(INT_ERROR_STATUS,
					"Grouper batch job had  a  problem creating relations: "+e.getLocalizedMessage());
			results.setUserNote(errorInfo.getMessage());
    }
  }

  public ArrayList<String> getObjectCSIDs(ArrayList<String> displayNames) throws URISyntaxException {
    ArrayList<String> csids = new ArrayList<String>();

    CollectionObjectResource collectionObjectResource = (CollectionObjectResource) getResourceMap().get(CollectionObjectClient.SERVICE_NAME);

    for (String displayName : displayNames) {
      UriInfo uriInfo = createKeywordSearchUriInfo("collectionobjects_common", "objectNumber", displayName.trim()); // trim it
      logger.warn("Searching for record: " + uriInfo.toString());

      AbstractCommonList objectList = collectionObjectResource.getList(getServiceContext(), uriInfo);

      for (AbstractCommonList.ListItem item : objectList.getListItem()) {
        for (org.w3c.dom.Element element : item.getAny()) {
          if (element.getTagName().equals("csid")) {
            String csid = element.getTextContent();

            if (!csids.contains(csid) && csid != null) {
              csids.add(csid);
            } else {
              logger.warn("The csid " + csid + " was skipped, as it was a duplicate.");
            }

            break;
          }
        }
      }
    }

    return csids;
  }
}