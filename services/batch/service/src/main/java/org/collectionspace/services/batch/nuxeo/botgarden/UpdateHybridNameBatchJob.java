package org.collectionspace.services.batch.nuxeo.botgarden;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Iterator;

import org.dom4j.Element;
import org.dom4j.Node;
import org.collectionspace.services.batch.nuxeo.AbstractBatchJob;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;
import org.collectionspace.services.taxonomy.nuxeo.TaxonConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateHybridNameBatchJob extends AbstractBatchJob {
    final Logger logger = LoggerFactory.getLogger(UpdateHybridNameBatchJob.class);

    public UpdateHybridNameBatchJob() {
        setSupportedInvocationModes(Collections.singletonList(INVOCATION_MODE_LIST));
    }

    @Override
    public void run() {
        setCompletionStatus(0);

        try {
            String mode = getInvocationContext().getMode();

            if (mode.equalsIgnoreCase(INVOCATION_MODE_LIST)) {
                List<String> csids = getInvocationContext().getListCSIDs().getCsid();
                setResults(updateHybridNames(csids));
            } else {
                throw new Exception("Unsupported invocation mode: " + mode);
            }

        } catch (Exception e) {
            setCompletionStatus(STATUS_ERROR);
            setErrorInfo(new InvocationError(INT_ERROR_STATUS, e.getMessage()));
        }
    }

    private InvocationResults updateHybridNames(List<String> csids) throws Exception {
        InvocationResults results = new InvocationResults();
        int numAffected = 0;

        for (String csid : csids) {
            updateRecord(csid);

            numAffected += 1;
        }

        results.setNumAffected(numAffected);
        return results;
    }

    private void updateRecord(String csid) throws Exception {
        PoxPayloadOut currentRecord = findCollectionObjectByCsid(csid);

        // Get the GroupList element
        Element naturalHistoryElement = currentRecord.getPart("collectionobjects_naturalhistory").asElement();
        Element taxonIdentGroupList = null;
        Iterator<Element> childIterator = naturalHistoryElement.elementIterator();

        // so now we have  our termGroupList element
        while (childIterator.hasNext()) {
            Element candidateElement = childIterator.next();

            // note: the candidateElement at some point is the "hybridParentGroupList"
            if (candidateElement.getName().contains("taxonomicIdentGroupList")) {
                taxonIdentGroupList = candidateElement;
            }
        }

        if (taxonIdentGroupList == null) {
            throw new Exception("No lists to update on record with csid " + csid);
        }

        ArrayList<Element> updatedTermGroups = new ArrayList<Element>();
        childIterator = taxonIdentGroupList.elementIterator();

        // now to iterate through each term group, and key and prepare the payload, put it in a list
        while (childIterator.hasNext()) {
            Element termGroupElement = childIterator.next();
            updateElement(termGroupElement);
        }
        String payload = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                            "<document name=\"collectionobjects\">" +
                                "<ns2:collectionobjects_naturalhistory xmlns:ns2=\"http://collectionspace.org/services/collectionobject/domain/naturalhistory\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
                                    taxonIdentGroupList.asXML() +
                                "</ns2:collectionobjects_naturalhistory>" +
                            "</document>";

        ResourceMap resource = getResourceMap();
        NuxeoBasedResource collectionObjectResource = (NuxeoBasedResource) resource.get(CollectionObjectClient.SERVICE_NAME);
        byte[] responseBytes = collectionObjectResource.update(getServiceContext(), resource, createUriInfo(), csid, payload);

        logger.debug("Batch resource: Response from CollectionObject (cataloging record) update: {}",
                     new String(responseBytes));
    }

    public void updateElement(Element termGroupElement) {
        String taxonomicIdentHybridName = "";

        Node hybridFlagNode = termGroupElement.selectSingleNode(TaxonConstants.HYBRID_FLAG);
        String affinityTaxon = termGroupElement.selectSingleNode(TaxonConstants.AFF_TAXON).getText();
        affinityTaxon = affinityTaxon.isEmpty() ? "" : RefNameUtils.getDisplayName(affinityTaxon);

        String taxon = termGroupElement.selectSingleNode(TaxonConstants.TAXON_NAME).getText();
        taxon = taxon.isEmpty() ? "" : RefNameUtils.getDisplayName(taxon);


        if (!hybridFlagNode.getText().equals("true")) {
            if (affinityTaxon == null) {
                taxonomicIdentHybridName = taxon;
            } if (affinityTaxon != null) {
                taxonomicIdentHybridName = affinityTaxon;
            }
        } else {
            Iterator<Element> hybridParentGroupListIterator = termGroupElement.elementIterator();
            Element hybridParentGroupList = null;

            // get the hybrid parent group list
            while (hybridParentGroupListIterator.hasNext()) {
                Element candidateElement = hybridParentGroupListIterator.next();

                if (candidateElement.getName().contains(TaxonConstants.TAXON_HYBRID_PARENT_GROUP_LIST)) {
                    hybridParentGroupList = candidateElement;
                }
            }

            List<Node> hybridParentGroup = hybridParentGroupList.selectNodes(TaxonConstants.TAXON_HYBRID_PARENT_GROUP);

            if (hybridParentGroup.size() != 2) {
                return;
            }

            Node firstParentNode = hybridParentGroup.get(0);
            Node secondParentNode =hybridParentGroup.get(1);

            String firstParentSex = firstParentNode.selectSingleNode(TaxonConstants.TAXON_HYBRID_PARENT_QUALIF).getText();

            Node maleParentNode = firstParentSex.equals("male") ? firstParentNode : secondParentNode;
            Node femaleParentNode = firstParentSex.equals("female") ? firstParentNode : secondParentNode;


            String maleParentName = RefNameUtils.getDisplayName(maleParentNode.selectSingleNode(TaxonConstants.TAXON_HYBRID_PARENT).getText());
            String femaleParentName = RefNameUtils.getDisplayName(femaleParentNode.selectSingleNode(TaxonConstants.TAXON_HYBRID_PARENT).getText());

            int maleParentGenusIndex = maleParentName.indexOf(' ');

            String maleParentGenus = maleParentGenusIndex != -1 ? maleParentName.substring(0, maleParentGenusIndex) : maleParentName;
            String femaleParentGenus = femaleParentName.indexOf(' ') != -1 ? femaleParentName.substring(0, femaleParentName.indexOf(' ')) : femaleParentName;

            String maleParentRest = maleParentGenusIndex != -1 ? maleParentName.substring(maleParentGenusIndex + 1) : maleParentName;

            if (affinityTaxon == null || affinityTaxon.isEmpty()) {
                if (femaleParentName.isEmpty()) {
                    taxonomicIdentHybridName = "";
                } else if (maleParentName.isEmpty()) {
                    taxonomicIdentHybridName = "";
                } else if (femaleParentGenus.equals(maleParentGenus)) {
                    taxonomicIdentHybridName = femaleParentName + " × " + maleParentGenus.charAt(0) + ". " + maleParentRest;
                } else {
                    taxonomicIdentHybridName = femaleParentName + " × " + maleParentName;
                }
            } else {
                if (maleParentName.isEmpty()) {
                    taxonomicIdentHybridName = "";
                } else if (femaleParentGenus.equals(maleParentGenus)) {
                    taxonomicIdentHybridName = affinityTaxon + " × " + maleParentGenus.charAt(0) + ". " + maleParentRest;
                } else {
                    taxonomicIdentHybridName = affinityTaxon + " × " + maleParentName;
                }
            }
        }

        Node hybridNameNode = termGroupElement.selectSingleNode(TaxonConstants.TAXON_HYBRID_NAME);
        hybridNameNode.setText(taxonomicIdentHybridName) ;
    }
}