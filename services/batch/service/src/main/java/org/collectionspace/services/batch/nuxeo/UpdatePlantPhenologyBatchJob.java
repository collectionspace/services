package org.collectionspace.services.batch.nuxeo;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.client.TaxonomyAuthorityClient;
import org.collectionspace.services.client.workflow.WorkflowClient;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectBotGardenConstants;
import org.collectionspace.services.collectionobject.nuxeo.CollectionObjectConstants;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.api.RefName;
import org.collectionspace.services.common.invocable.InvocationContext.ListCSIDs;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.taxonomy.nuxeo.TaxonBotGardenConstants;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdatePlantPhenologyBatchJob extends AbstractBatchJob {
  final Logger logger = LoggerFactory.getLogger(UpdatePlantPhenologyBatchJob.class);
  
  public UpdatePlantPhenologyBatchJob() {
		this.setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_SINGLE, INVOCATION_MODE_LIST));
  }
  

  @Override
  public void run() {

    try {
      InvocationContext ctx = getInvocationContext()
      String mode = ctx.getMode();

      if (mode.equalsIgnoreCase(INVOCATION_MODE_SINGLE)) {
        
        String csid = ctx.getSingleCSID();

      } else if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
        ListCSIDs csids = ctx.getListCSIDs();
      } else {
        throw new Exception("Unsupported invocation mode: " + mode);
      }

    } catch (Exception e) {
      // setCompletionStatus(STATUS_ERROR);
			setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
    }
  }
}