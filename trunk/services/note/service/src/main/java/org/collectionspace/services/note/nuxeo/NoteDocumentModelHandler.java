/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 Regents of the University of California

 *  Licensed under the Educational Community License (ECL), Version 2.0.
 *  You may not use this file except in compliance with this License.

 *  You may obtain a copy of the ECL 2.0 License at

 *  https://source.collectionspace.org/collection-space/LICENSE.txt

 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.note.nuxeo;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.note.NoteJAXBSchema;
import org.collectionspace.services.common.document.DocumentHandler.Action;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.note.NotesCommon;
import org.collectionspace.services.note.NotesCommonList;
import org.collectionspace.services.note.NotesCommonList.NoteListItem;

import org.collectionspace.services.nuxeo.client.java.RemoteDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.client.java.RemoteSubItemDocumentModelHandlerImpl;
import org.collectionspace.services.nuxeo.util.NuxeoUtils;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class NoteDocumentModelHandler.
 */
public class NoteDocumentModelHandler
        extends RemoteSubItemDocumentModelHandlerImpl<NotesCommon, NotesCommonList> {

    /** The logger. */
    private final Logger logger = LoggerFactory.getLogger(NoteDocumentModelHandler.class);
    
    /** The note. */
    private NotesCommon note;
    
    /** The note list. */
    private NotesCommonList noteList;

    /** The owner. */
    private String owner;
    
    private final String commonSchemaName = "notes_common";
    
	public boolean schemaHasSubItem(String schema) {
		return commonSchemaName.equals(schema);
	}


    /**
     * Gets the owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Sets the owner.
     *
     * @param owner the new owner
     */
    public void setInAuthority(String owner) {
        this.owner = owner;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getCommonPart()
     */
    @Override
    public NotesCommon getCommonPart() {
        return note;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPart(java.lang.Object)
     */
    @Override
    public void setCommonPart(NotesCommon note) {
        this.note = note;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#getCommonPartList()
     */
    @Override
    public NotesCommonList getCommonPartList() {
        return noteList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#setCommonPartList(java.lang.Object)
     */
    @Override
    public void setCommonPartList(NotesCommonList noteList) {
        this.noteList = noteList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPart(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public NotesCommon extractCommonPart(DocumentWrapper<DocumentModel> wrapDoc)
            throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#fillCommonPart(java.lang.Object, org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public void fillCommonPart(NotesCommon noteObject, DocumentWrapper<DocumentModel> wrapDoc) throws Exception {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.nuxeo.client.java.DocumentModelHandler#extractCommonPartList(org.collectionspace.services.common.document.DocumentWrapper)
     */
    @Override
    public NotesCommonList extractCommonPartList(DocumentWrapper<DocumentModelList> wrapDoc) throws Exception {
        NotesCommonList coList = extractPagingInfo(new NotesCommonList(), wrapDoc);
        List<NotesCommonList.NoteListItem> list = coList.getNoteListItem();
        Iterator<DocumentModel> iter = wrapDoc.getWrappedObject().iterator();
        while(iter.hasNext()){
            DocumentModel docModel = iter.next();
            NoteListItem clistItem = new NoteListItem();
            clistItem.setContent((String) docModel.getProperty(getServiceContext().getCommonPartLabel(),
                    NoteJAXBSchema.CONTENT));
            String id = getCsid(docModel);//NuxeoUtils.extractId(docModel.getPathAsString());
            clistItem.setUri(getServiceContextPath() + id);
            clistItem.setCsid(id);
            list.add(clistItem);
        }

        return coList;
    }

    /* (non-Javadoc)
     * @see org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl#getQProperty(java.lang.String)
     */
    @Override
    public String getQProperty(String prop) {
        return NoteConstants.NUXEO_SCHEMA_NAME + ":" + prop;
    }
}

