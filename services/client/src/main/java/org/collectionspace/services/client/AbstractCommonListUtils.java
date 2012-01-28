package org.collectionspace.services.client;

import java.util.List;

import org.collectionspace.services.jaxb.AbstractCommonList;
import org.slf4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class AbstractCommonListUtils {
    public static void ListItemsInAbstractCommonList(
    		AbstractCommonList list, Logger logger, String testName) {
    	List<AbstractCommonList.ListItem> items =
    		list.getListItem();
    	int i = 0;
    	for(AbstractCommonList.ListItem item : items){
    		List<Element> elList = item.getAny();
    		StringBuilder elementStrings = new StringBuilder();
    		for(Element el : elList) {
    			Node textEl = el.getFirstChild();
   				elementStrings.append("["+el.getNodeName()+":"+((textEl!=null)?textEl.getNodeValue():"NULL")+"] ");
    		}
    		logger.debug(testName + ": list-item[" + i + "]: "+elementStrings.toString());
    		i++;
    	}
    }

    public static String ListItemGetCSID(AbstractCommonList.ListItem item) {
		return ListItemGetElementValue(item, "csid");
	}

    public static String ListItemGetElementValue(AbstractCommonList.ListItem item,
    		String elName) {
		List<Element> elList = item.getAny();
		for(Element el : elList) {
			if(elName.equalsIgnoreCase(el.getNodeName())) {
    			Node textEl = el.getFirstChild();
    			return textEl.getNodeValue();
			}
		}
		return null;
	}



}
