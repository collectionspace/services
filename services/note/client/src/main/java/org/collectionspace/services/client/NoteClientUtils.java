package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.note.NotesCommon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteClientUtils {

    private static final Logger logger =
        LoggerFactory.getLogger(NoteClientUtils.class);

    private static final String SERVICE_PATH_COMPONENT = "notes";
  
    public static PoxPayloadOut createNoteInstance (
        String owner, String identifier, String headerLabel) {
        return createNoteInstance(
            owner,
						false, 0,
            "content-" + identifier,
            "author-" + identifier,
            "date-" + identifier,
            headerLabel);
    }

    public static PoxPayloadOut createNoteInstance(
        String owner, boolean isPrimary, int order,
        String content, String author, String date, String headerLabel) {
        NotesCommon noteCommon = new NotesCommon();
        noteCommon.setOwner(owner);
        noteCommon.setIsPrimary(isPrimary);
        noteCommon.setOrder(order);
        noteCommon.setContent(content);
        noteCommon.setAuthor(author);
        noteCommon.setDate(date);

        PoxPayloadOut multipart = new PoxPayloadOut(getServicePathComponent());
        PayloadOutputPart commonPart =
                multipart.addPart(noteCommon, MediaType.APPLICATION_XML_TYPE);
        commonPart.setLabel(new NoteClient().getCommonPartName());

        if(logger.isDebugEnabled()){
            logger.debug("to be created, note common");
            // logger.debug(objectAsXmlString(note, NotesCommon.class));
        }

        return multipart;
    }

    public static String getServicePathComponent() {
        return SERVICE_PATH_COMPONENT;
    }

}
