package org.collectionspace.services.batch.nuxeo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;

import org.apache.commons.lang.StringUtils;
import org.collectionspace.services.client.CollectionObjectClient;
import org.collectionspace.services.client.CollectionObjectFactory;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.collectionobject.CollectionobjectsCommon;
import org.collectionspace.services.common.api.RefNameUtils;
import org.collectionspace.services.common.api.RefNameUtils.AuthorityTermInfo;
import org.collectionspace.services.common.invocable.InvocationContext.Params.Param;
import org.dom4j.Element;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.collectionspace.services.common.invocable.InvocationResults;
import org.collectionspace.services.common.NuxeoBasedResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.collectionspace.services.client.PayloadOutputPart;
import org.collectionspace.services.client.PoxPayload;
import org.collectionspace.services.client.PayloadPart;

import org.collectionspace.services.common.NuxeoBasedResource;
import org.collectionspace.services.common.ResourceMap;


public class UpdateHybridNameBatchJob extends AbstractBatchJob {
    final Logger logger = LoggerFactory.getLogger(UpdateHybridNameBatchJob.class);

    public UpdateHybridNameBatchJob() {
        setSupportedInvocationModes(Arrays.asList(INVOCATION_MODE_LIST));
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
        // Need to get _THIS_ csid's attributes findCollectionObjectByCsid
        PoxPayloadOut currentRecord = findCollectionObjectByCsid(csid);

        ArrayList<String> hybridNames = new ArrayList<String>();

        // Get the GroupList element
        Element naturalHistoryElement = currentRecord.getPart("collectionobjects_naturalhistory").asElement();
        Element parentGroupList = null;
        Element taxonIdentGroupList = null;
        Iterator<Element> childIterator = naturalHistoryElement.elementIterator();

        // so now we have  our termGroupList element
        while (childIterator.hasNext()) {
            Element candidateElement = childIterator.next();

            // note: the candidateElement at some point is the "hybridParentGroupList"
            if (candidateElement.getName().contains("taxonomicIdentGroupList")) {
                taxonIdentGroupList = candidateElement;
            }
            if (candidateElement.getName().contains("hybridParentGroupList")) {
                parentGroupList = candidateElement;
            }
        }

        if (taxonIdentGroupList == null) {
            throw new Exception("Blah");
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
        
        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Batch resource: Resonse from collectionobject (cataloging record) update: %s", new String(responseBytes)));
        }
        
        // String serviceName = getAuthorityServiceNameForDocType("collectionobject");
        // AuthorityResource<?, ?> resource = (AuthorityResource<?, ?>) getResourceMap().get(serviceName);
        
		// resource.updateAuthorityItem(getServiceContext(), getResourceMap(), createUriInfo(), inAuthority, csid, payload);


    }


	// private String getUpdatePayload(Element originalTermGroupListElement, Element updatedTermGroupListElement) {
	// 	List<Element> parents = new ArrayList<Element>();

	// 	for (Element e = originalTermGroupListElement; e != null; e = e.getParent()) {
	// 		parents.add(e);
	// 	}

	// 	Collections.reverse(parents);

	// 	// Remove the original termGroupList element
	// 	parents.remove(parents.size() - 1);

	// 	// Remove the root
	// 	Element rootElement = parents.remove(0);

	// 	// Copy the root to a new document
	// 	Document document = DocumentHelper.createDocument(copyElement(rootElement));
	// 	Element current = document.getRootElement();

	// 	// Copy the remaining parents
	// 	for (Element parent : parents) {
	// 		Element parentCopy = copyElement(parent);

	// 		current.add(parentCopy);
	// 		current = parentCopy;
	// 	}

	// 	// Add the updated termGroupList element

	// 	current.add(updatedTermGroupListElement);

	// 	String payload = document.asXML();

	// 	return payload;
	// }


    public void updateElement(Element termGroupElement) {
        int x = 0;
        String taxonomicIdentHybridName = "";

        Node hybridFlagNode = termGroupElement.selectSingleNode("hybridFlag");
        String affinityTaxon = termGroupElement.selectSingleNode("affinityTaxon").getText();
        affinityTaxon = affinityTaxon.equals("") ? "" : RefNameUtils.getDisplayName(affinityTaxon);

        String taxon = termGroupElement.selectSingleNode("taxon").getText();
        taxon = taxon.equals("") ? "" : RefNameUtils.getDisplayName(taxon);


        if (hybridFlagNode.getText() != "true") {
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

                if (candidateElement.getName().contains("taxonomicIdentHybridParentGroupList")) {
                    hybridParentGroupList = candidateElement;
                }
            }

            List<Node> hybridParentGroup = hybridParentGroupList.selectNodes("taxonomicIdentHybridParentGroup");

            if (hybridParentGroup.size() != 2) {
                return;
            }

            Node firstParentNode = hybridParentGroup.get(0);
            Node secondParentNode =hybridParentGroup.get(1);

            String firstParentSex = firstParentNode.selectSingleNode("taxonomicIdentHybridParentQualifier").getText();

            String secondParentSex = secondParentNode.selectSingleNode("taxonomicIdentHybridParentQualifier").getText();

            Node maleParentNode = firstParentSex.equals("male") ? firstParentNode : secondParentNode;
            Node femaleParentNode = firstParentSex.equals("female") ? firstParentNode : secondParentNode;


            String maleParentName = RefNameUtils.getDisplayName(maleParentNode.selectSingleNode("taxonomicIdentHybridParent").getText());
            String femaleParentName = RefNameUtils.getDisplayName(femaleParentNode.selectSingleNode("taxonomicIdentHybridParent").getText());

            int maleParentGenusIndex = maleParentName.indexOf(' ');
            
            String maleParentGenus = maleParentGenusIndex != -1 ? maleParentName.substring(0, maleParentGenusIndex) : maleParentName;
            String femaleParentGenus = femaleParentName.indexOf(' ') != -1 ? femaleParentName.substring(0, femaleParentName.indexOf(' ')) : femaleParentName;

            String maleParentRest = maleParentGenusIndex != -1 ? maleParentName.substring(maleParentGenusIndex + 1) : maleParentName;

            if (affinityTaxon == null || affinityTaxon.equals("")) {
                if (femaleParentName == null || femaleParentName.equals("")) {
                    taxonomicIdentHybridName = "";
                } else if (maleParentName == null || maleParentName.equals("")) {
                    taxonomicIdentHybridName = "";
                } else if (femaleParentGenus.equals(maleParentGenus)) {
                    taxonomicIdentHybridName = femaleParentName + " × " + maleParentGenus.substring(0,1) + ". " + maleParentRest;
                } else {
                    taxonomicIdentHybridName = femaleParentName + " × " + maleParentName;
                }
            } else {
                if (maleParentName == null || maleParentName.equals("")) {
                    taxonomicIdentHybridName = "";
                } else if (femaleParentGenus.equals(maleParentGenus)) {
                    taxonomicIdentHybridName = affinityTaxon + " × " + maleParentGenus.substring(0,1) + ". " + maleParentRest;
                } else {
                    taxonomicIdentHybridName = affinityTaxon + " × " + maleParentName;
                }
                
            }



        }

        Node hybridNameNode = termGroupElement.selectSingleNode("taxonomicIdentHybridName");
        hybridNameNode.setText(taxonomicIdentHybridName) ;

        // if Node displayNameNode = termGroupElement.selectSingleNode("termDisplayName");

    }



    // private Element getTermGroupListElement(PoxPayloadOut itemPayload) {
    //     currentRecord.getPart("collectionobjects_naturalhistory").asElement()
	// 	Element termGroupListElement = null;
	// 	Element commonPartElement = findCommonPartElement(itemPayload);

	// 	if (commonPartElement != null) {
	// 		termGroupListElement = findTermGroupListElement(commonPartElement);
	// 	}

	// 	return termGroupListElement;
	// }
}