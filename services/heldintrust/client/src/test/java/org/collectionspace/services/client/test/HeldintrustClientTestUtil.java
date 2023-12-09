package org.collectionspace.services.client.test;

import java.util.List;

import org.collectionspace.services.heldintrust.CorrespondenceGroup;
import org.collectionspace.services.heldintrust.CorrespondenceGroupList;
import org.collectionspace.services.heldintrust.ExternalApprovalGroup;
import org.collectionspace.services.heldintrust.ExternalApprovalGroupList;
import org.collectionspace.services.heldintrust.HeldInTrustDepositorGroup;
import org.collectionspace.services.heldintrust.HeldInTrustDepositorGroupList;
import org.collectionspace.services.heldintrust.HeldintrustsCommon;
import org.collectionspace.services.heldintrust.InternalApprovalGroup;
import org.collectionspace.services.heldintrust.InternalApprovalGroupList;


public class HeldintrustClientTestUtil {

    protected static HeldintrustsCommon createHitInstance(
        String heldInTrustNumber,
        String depositorContact,
        String depositor,
        String externalApprovalIndividual,
        String correspondenceSender) {
        HeldintrustsCommon heldInTrust = new HeldintrustsCommon();

        heldInTrust.setHeldInTrustNumber(heldInTrustNumber);

        HeldInTrustDepositorGroupList tempHDGL = heldInTrust.getHeldInTrustDepositorGroupList();
        if (tempHDGL == null) {
            tempHDGL = new HeldInTrustDepositorGroupList();
        }
        List<HeldInTrustDepositorGroup> hitDepositorGroupList = tempHDGL.getHeldInTrustDepositorGroup();
        HeldInTrustDepositorGroup hitDepositorGroup = new HeldInTrustDepositorGroup();
        hitDepositorGroup.setDepositor(depositor);
        hitDepositorGroup.setDepositorContact(depositorContact);
        hitDepositorGroupList.add(hitDepositorGroup);
        heldInTrust.setHeldInTrustDepositorGroupList(tempHDGL);

        InternalApprovalGroupList tempIAGL = heldInTrust.getInternalApprovalGroupList();
        if (tempIAGL == null) {
            tempIAGL = new InternalApprovalGroupList();
        }
        List<InternalApprovalGroup> internalApprovalGroupList = tempIAGL.getInternalApprovalGroup();
        InternalApprovalGroup internalApprovalGroup = new InternalApprovalGroup();
        internalApprovalGroup.setInternalApprovalIndividual(depositor);
        internalApprovalGroupList.add(internalApprovalGroup);
        heldInTrust.setInternalApprovalGroupList(tempIAGL);

        ExternalApprovalGroupList tempEAGL = heldInTrust.getExternalApprovalGroupList();
        if (tempEAGL == null) {
            tempEAGL = new ExternalApprovalGroupList();
        }
        List<ExternalApprovalGroup> externalApprovalGroupList = tempEAGL.getExternalApprovalGroup();
        ExternalApprovalGroup externalApprovalGroup = new ExternalApprovalGroup();
        externalApprovalGroup.setExternalApprovalIndividual(externalApprovalIndividual);
        externalApprovalGroupList.add(externalApprovalGroup);
        heldInTrust.setExternalApprovalGroupList(tempEAGL);

        CorrespondenceGroupList tempCGL = heldInTrust.getCorrespondenceGroupList();
        if (tempCGL == null) {
            tempCGL = new CorrespondenceGroupList();
        }
        List<CorrespondenceGroup> correspondanceGroupList = tempCGL.getCorrespondenceGroup();
        CorrespondenceGroup correspondenceGroup = new CorrespondenceGroup();
        correspondenceGroup.setCorrespondenceSender(correspondenceSender);
        correspondanceGroupList.add(correspondenceGroup);
        heldInTrust.setCorrespondenceGroupList(tempCGL);

        return heldInTrust;
    }

}
