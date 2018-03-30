package org.collectionspace.services.systeminfo;

import java.util.List;

import org.collectionspace.services.common.api.RefName.RefNameInterface;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.document.AbstractMultipartDocumentHandlerImpl;
import org.collectionspace.services.common.document.DocumentException;
import org.collectionspace.services.common.document.DocumentFilter;
import org.collectionspace.services.common.document.DocumentWrapper;
import org.collectionspace.services.lifecycle.Lifecycle;
import org.collectionspace.services.lifecycle.TransitionDef;
import org.nuxeo.ecm.core.api.DocumentModel;

/*
 * The StructedDate service uses non of these method.  It exists only because it is needed to create a proper ServiceContext instance.
 */
public class SystemInfoDocumentHandler extends AbstractMultipartDocumentHandlerImpl<SystemInfoCommon, List<SystemInfoCommon>, SystemInfoCommon, List<SystemInfoCommon>> {

	@Override
	public Lifecycle getLifecycle() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public Lifecycle getLifecycle(String serviceObjectName) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleWorkflowTransition(ServiceContext ctx, DocumentWrapper<DocumentModel> wrapDoc,
			TransitionDef transitionDef) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleCreate(DocumentWrapper<SystemInfoCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleUpdate(DocumentWrapper<SystemInfoCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleGet(DocumentWrapper<SystemInfoCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void handleGetAll(DocumentWrapper<List<SystemInfoCommon>> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void extractAllParts(DocumentWrapper<SystemInfoCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void fillAllParts(DocumentWrapper<SystemInfoCommon> wrapDoc,
			org.collectionspace.services.common.document.DocumentHandler.Action action) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public SystemInfoCommon extractCommonPart(DocumentWrapper<SystemInfoCommon> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void fillCommonPart(SystemInfoCommon obj, DocumentWrapper<SystemInfoCommon> wrapDoc)
			throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<SystemInfoCommon> extractCommonPartList(DocumentWrapper<List<SystemInfoCommon>> wrapDoc)
			throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<SystemInfoCommon> extractPagingInfo(List<SystemInfoCommon> theCommonList,
			DocumentWrapper<List<SystemInfoCommon>> wrapDoc) throws Exception {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public SystemInfoCommon getCommonPart() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void setCommonPart(SystemInfoCommon obj) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public List<SystemInfoCommon> getCommonPartList() {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public void setCommonPartList(List<SystemInfoCommon> obj) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public String getQProperty(String prop) throws DocumentException {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	protected String getRefnameDisplayName(DocumentWrapper<SystemInfoCommon> docWrapper) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	protected RefNameInterface getRefName(DocumentWrapper<SystemInfoCommon> docWrapper, String tenantName,
			String serviceName) {
		throw new RuntimeException("Unimplemented method.");
	}

	@Override
	public DocumentFilter createDocumentFilter() {
		throw new RuntimeException("Unimplemented method.");
	}
	
	@Override
	public boolean supportsWorkflowStates() {
		return false;
	}
}
