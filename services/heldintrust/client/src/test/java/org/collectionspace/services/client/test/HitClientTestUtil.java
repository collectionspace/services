package org.collectionspace.services.client.test;

import java.util.List;

import org.collectionspace.services.hit.CorrespondenceGroup;
import org.collectionspace.services.hit.CorrespondenceGroupList;
import org.collectionspace.services.hit.ExternalApprovalGroup;
import org.collectionspace.services.hit.ExternalApprovalGroupList;
import org.collectionspace.services.hit.HitDepositorGroup;
import org.collectionspace.services.hit.HitDepositorGroupList;
import org.collectionspace.services.hit.HitsCommon;
import org.collectionspace.services.hit.InternalApprovalGroup;
import org.collectionspace.services.hit.InternalApprovalGroupList;

public class HitClientTestUtil {

    protected static HitsCommon createHitInstance(
        String heldInTrustNumber,
        String depositorContact,
        String depositor,
        String externalApprovalIndividual,
        String correspondenceSender) {
        HitsCommon hit = new HitsCommon();

        hit.setHitNumber(heldInTrustNumber);

        HitDepositorGroupList tempHDGL = hit.getHitDepositorGroupList();
        if (tempHDGL == null) {
            tempHDGL = new HitDepositorGroupList();
        }
        List<HitDepositorGroup> hitDepositorGroupList = tempHDGL.getHitDepositorGroup();
        HitDepositorGroup hitDepositorGroup = new HitDepositorGroup();
        hitDepositorGroup.setDepositor(depositor);
        hitDepositorGroup.setDepositorContact(depositorContact);
        hitDepositorGroupList.add(hitDepositorGroup);
        hit.setHitDepositorGroupList(tempHDGL);

        InternalApprovalGroupList tempIAGL = hit.getInternalApprovalGroupList();
        if (tempIAGL == null) {
            tempIAGL = new InternalApprovalGroupList();
        }
        List<InternalApprovalGroup> internalApprovalGroupList = tempIAGL.getInternalApprovalGroup();
        InternalApprovalGroup internalApprovalGroup = new InternalApprovalGroup();
        internalApprovalGroup.setInternalApprovalIndividual(depositor);
        internalApprovalGroupList.add(internalApprovalGroup);
        hit.setInternalApprovalGroupList(tempIAGL);

        ExternalApprovalGroupList tempEAGL = hit.getExternalApprovalGroupList();
        if (tempEAGL == null) {
            tempEAGL = new ExternalApprovalGroupList();
        }
        List<ExternalApprovalGroup> externalApprovalGroupList = tempEAGL.getExternalApprovalGroup();
        ExternalApprovalGroup externalApprovalGroup = new ExternalApprovalGroup();
        externalApprovalGroup.setExternalApprovalIndividual(externalApprovalIndividual);
        externalApprovalGroupList.add(externalApprovalGroup);
        hit.setExternalApprovalGroupList(tempEAGL);

        CorrespondenceGroupList tempCGL = hit.getCorrespondenceGroupList();
        if (tempCGL == null) {
            tempCGL = new CorrespondenceGroupList();
        }
        List<CorrespondenceGroup> correspondanceGroupList = tempCGL.getCorrespondenceGroup();
        CorrespondenceGroup correspondenceGroup = new CorrespondenceGroup();
        correspondenceGroup.setCorrespondenceSender(correspondenceSender);
        correspondanceGroupList.add(correspondenceGroup);
        hit.setCorrespondenceGroupList(tempCGL);

        return hit;
    }

}
