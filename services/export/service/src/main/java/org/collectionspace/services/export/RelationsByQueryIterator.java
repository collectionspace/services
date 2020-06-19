
package org.collectionspace.services.export;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.relation.RelationsCommonList;
import org.collectionspace.services.relation.RelationsCommonList.RelationListItem;

public class RelationsByQueryIterator extends AbstractDocumentsByQueryIterator<RelationListItem> implements Iterator<PoxPayloadOut> {
  RelationsByQueryIterator(
    ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    String docType,
    String vocabulary,
    InvocationContext.Query query) throws Exception {

    super(serviceContext, docType, vocabulary, query);
  }

  @Override
  protected List<RelationListItem> getListItems(AbstractCommonList list) {
    return ((RelationsCommonList) list).getRelationListItem();
  }

  @Override
  protected String getListItemCsid(RelationListItem listItem) {
    return listItem.getCsid();
  }
}
