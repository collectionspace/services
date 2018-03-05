package org.collectionspace.services.common.authorization_mgt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.collectionspace.services.authorization.perms.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionGroup {
    final static Logger logger = LoggerFactory.getLogger(ActionGroup.class);

	String name;
	ActionType[] actions = {};
	
	static private String toString(ActionType[] actionTypes) {
		String result = null;
		
		if (actionTypes.length > 0) {
			result = new String();
			for (ActionType actionType: actionTypes) {
				result = result + actionType.value() + ':';
			}
		}
		
		return result;
	}
	
	static private String toString(List<Character> charList) {
		String result = null;
		
		if (charList.isEmpty() == false) {
			result = new String();
			for (Character c: charList) {
				result = result + c;
			}
		}
		
		return result;
	}
	
	static int valueOf(Character c) {
		int result = 0;
		
		switch (c) {
		case 'C':
			result = 1;
			break;
		case 'R':
			result = 2;
			break;
		case 'U':
			result = 3;
			break;
		case 'D':
			result = 4;
			break;
		case 'I':
			result = 5;
			break;
		case 'L':
			result = 6;
			break;
		default:
			result = 0;
		}
		
		return result;
	}
	
	static int valueOf(ActionType actionType) {
		int result = 0;
		
		switch (actionType) {
		case CREATE:
			result = 1;
			break;
		case READ:
			result = 2;
			break;
		case UPDATE:
			result = 3;
			break;
		case DELETE:
			result = 4;
			break;
		case RUN:
			result = 5;
			break;
		case SEARCH:
			result = 6;
			break;
		default:
			result = 0;
		}
		
		return result;
	}

	/**
	 * Factory method to create an ActionGroup from an action string -i.e., "CRUDL", "RL", "CRUL", etc.
	 * @param actionString
	 * @return
	 */
	static public ActionGroup creatActionGroup(String actionString) {
		ActionGroup result = null;
		
		Set<Character> actionCharSet = new HashSet<Character>();
		Set<ActionType> actionTypeSet = new HashSet<ActionType>();
		for (char c : actionString.toCharArray()) {
			switch (c) {
			case 'C':
				actionTypeSet.add(ActionType.CREATE);
				actionCharSet.add(c);
				break;
				
			case 'R':
				actionTypeSet.add(ActionType.READ);
				actionCharSet.add(c);
				break;
				
			case 'U':
				actionTypeSet.add(ActionType.UPDATE);
				actionCharSet.add(c);
				break;
				
			case 'D':
				actionTypeSet.add(ActionType.DELETE);
				actionCharSet.add(c);
				break;
				
			case 'I':
				actionTypeSet.add(ActionType.RUN);
				actionCharSet.add(c);
				break;
				
			case 'L':
				actionTypeSet.add(ActionType.SEARCH);
				actionCharSet.add(c);
				break;
				
			default:
				System.out.println(String.format("Unknown action character '%c'.", c));
			}
		}
		
		if (actionTypeSet.size() > 0) {
			// sort for readability
			ArrayList<Character> actionCharList = new ArrayList<Character>(actionCharSet);
			Collections.sort(actionCharList, new Comparator<Character>() {
			    @Override
			    public int compare(Character c1, Character c2) {
			    	if (valueOf(c1) > valueOf(c2)) {
			    		return 1;
			    	} else if (valueOf(c1) < valueOf(c2)) {
			    		return -1;
			    	} else {
			    		return 0;
			    	}
			    }
			});
			
			// sort for readability
			ArrayList<ActionType> actionTypeList = new ArrayList<ActionType>(actionTypeSet);
			Collections.sort(actionTypeList, new Comparator<ActionType>() {
			    @Override
			    public int compare(ActionType a1, ActionType a2) {
			    	if (valueOf(a1) > valueOf(a2)) {
			    		return 1;
			    	} else if (valueOf(a1) < valueOf(a2)) {
			    		return -1;
			    	} else {
			    		return 0;
			    	}
			    }
			});
			
			result = new ActionGroup();
			result.name = toString(actionCharList);
			result.actions = actionTypeList.toArray(result.actions);
		}
		
		logger.trace(String.format("Create new action group containing these actions name:'%s' actions:'%s'",
				result.name, toString(result.actions)));			
		
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public ActionType[] getActions() {
		return this.actions;
	}
}
