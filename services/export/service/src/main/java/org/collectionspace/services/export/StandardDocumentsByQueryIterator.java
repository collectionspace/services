package org.collectionspace.services.export;

import java.util.Iterator;
import java.util.List;

import org.collectionspace.services.client.PoxPayloadIn;
import org.collectionspace.services.client.PoxPayloadOut;
import org.collectionspace.services.common.context.ServiceContext;
import org.collectionspace.services.common.invocable.InvocationContext;
import org.collectionspace.services.jaxb.AbstractCommonList;
import org.collectionspace.services.jaxb.AbstractCommonList.ListItem;

import org.w3c.dom.Element;

public class StandardDocumentsByQueryIterator extends AbstractDocumentsByQueryIterator<ListItem> implements Iterator<PoxPayloadOut> {
  StandardDocumentsByQueryIterator(
    ServiceContext<PoxPayloadIn, PoxPayloadOut> serviceContext,
    String docType,
    String vocabulary,
    InvocationContext.Query query) throws Exception {

    super(serviceContext, docType, vocabulary, query);
  }

  @Override
  protected List<ListItem> getListItems(AbstractCommonList list) {
    return list.getListItem();
  }

  @Override
  protected String getListItemCsid(ListItem listItem) {
    for (Element element : listItem.getAny()) {
      if (element.getTagName().equals("csid")) {
        return element.getTextContent();
      }
    }

    return null;
  }
}
