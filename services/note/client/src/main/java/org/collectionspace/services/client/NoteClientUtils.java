package org.collectionspace.services.client;

import javax.ws.rs.core.MediaType;

import org.collectionspace.services.client.NoteClient;
import org.collectionspace.services.note.NotesCommon;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.jboss.resteasy.plugins.providers.multipart.OutputPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoteClientUtils {

    private static final Logger logger =
        LoggerFactory.getLogger(NoteClientUtils.class);

    public static MultipartOutput createNoteInstance(
        String owner, String identifier, String headerLabel) {
        return createNoteInstance(
            owner,
						false, 0,
            "content-" + identifier,
            "author-" + identifier,
            "date-" + identifier,
            headerLabel);
    }

    public static MultipartOutput createNoteInstance(
        String owner, boolean isPrimary, int order,
        String content, String author, String date, String headerLabel) {
        NotesCommon note = new NotesCommon();
        note.setOwner(owner);
        note.setIsPrimary(isPrimary);
        note.setOrder(order);
        note.setContent(content);
        note.setAuthor(author);
        note.setDate(date);
        MultipartOutput multipart = new MultipartOutput();
        OutputPart commonPart =
            multipart.addPart(note, MediaType.APPLICATION_XML_TYPE);
        NoteClient client = new NoteClient();
        commonPart.getHeaders().add("label", headerLabel);

        if(logger.isDebugEnabled()){
            logger.debug("to be created, note common");
            // logger.debug(objectAsXmlString(note, NotesCommon.class));
        }

        return multipart;
    }


}
