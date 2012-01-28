/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright © 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.collectionspace.services.client.test;

import org.collectionspace.services.client.CollectionSpaceClient;
import org.collectionspace.services.client.NoteClient;
import org.collectionspace.services.client.NoteClientUtils;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.note.NotesCommon;
import org.collectionspace.services.note.NotesCommonList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * NoteServiceTest, carries out tests against a
 * deployed and running Note Service.
 *
 * $LastChangedRevision: 917 $
 * $LastChangedDate: 2009-11-06 12:20:28 -0800 (Fri, 06 Nov 2009) $
 */
public class NoteServiceTest extends AbstractPoxServiceTestImpl<NotesCommonList, NotesCommon> {

    private final String CLASS_NAME = NoteServiceTest.class.getName();
    private final Logger logger = LoggerFactory.getLogger(CLASS_NAME);

    private final String SERVICE_PATH_COMPONENT = "notes";
    private final String SERVICE_NAME = "notes";

    /* (non-Javadoc)
     * @see org.collectionspace.services.client.test.BaseServiceTest#getClientInstance()
     */
    @Override
    protected CollectionSpaceClient getClientInstance() {
    	return new NoteClient();
    }
    
	@Override
	protected PoxPayloadOut createInstance(String commonPartName,
			String identifier) {
        PoxPayloadOut result =
                NoteClientUtils.createNoteInstance("owner"+identifier, identifier, 
    								commonPartName);
        return result;
	}
    
    // ---------------------------------------------------------------
    // Utility tests : tests of code used in tests above
    // ---------------------------------------------------------------

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    // ---------------------------------------------------------------
    // Utility methods used by tests above
    // ---------------------------------------------------------------
    @Override
    public String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }
    
    @Override
    protected Class<NotesCommonList> getCommonListType() {
    	return NotesCommonList.class;
    }

	@Override
	protected NotesCommon updateInstance(NotesCommon notesCommon) {
		NotesCommon result = new NotesCommon();
		
        // Update the common part, both the subitem, and the content
        result.setContent("updated-" + notesCommon.getContent());
        result.setOrder(notesCommon.getOrder() + 10);
		
		return result;
	}

	@Override
	protected void compareUpdatedInstances(NotesCommon original,
			NotesCommon updated) throws Exception {
        // Check selected fields in the updated common part.
        Assert.assertEquals(updated.getContent(), original.getContent(),
                "Content in updated object did not match submitted data.");
        Assert.assertEquals(updated.getOrder(), original.getOrder(),
                "Order in updated object (subitem) did not match submitted data.");
	}
	
	@Override
	public void CRUDTests(String testName) {
		// TODO Auto-generated method stub
		
	}
}
