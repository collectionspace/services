/**
 *  This document is a part of the source code and related artifacts
 *  for CollectionSpace, an open source collections management system
 *  for museums and related institutions:

 *  http://www.collectionspace.org
 *  http://wiki.collectionspace.org

 *  Copyright 2009 University of California at Berkeley

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
package org.collectionspace.services.common.invocable;

import org.collectionspace.services.common.invocable.InvocationContext;
import java.util.List;


/**
 * Invocation defines an interface for invocable jobs (batch, reports, exports, etc)
 *
 * $LastChangedRevision: $
 * $LastChangedDate: $
 */
public interface Invocable {
	
	public class InvocationError {
		int responseCode;
		String message;
		
		public InvocationError(int responseCode, String message) {
			this.responseCode = responseCode;
			this.message = message;
		}

		public int getResponseCode() {
			return responseCode;
		}

		public void setResponseCode(int responseCode) {
			this.responseCode = responseCode;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}
	}

	public String INVOCATION_MODE_SINGLE = "single";
	public String INVOCATION_MODE_LIST = "list";
	public String INVOCATION_MODE_GROUP = "group";
	public String INVOCATION_MODE_NO_CONTEXT = "nocontext";
	//public String INVOCATION_MODE_QUERY = "query"; NYI

	public final int	STATUS_ERROR = -1;
	public final int	STATUS_UNSTARTED = 0;
	public final int	STATUS_MIN_PROGRESS = 1;
	public final int	STATUS_COMPLETE = 100;
	
	/**
	 * @return a set of modes that this plugin can support on invocation. Must be non-empty.
	 */
	public List<String> getSupportedInvocationModes();
	
	/**
	 * Sets the invocation context for the batch job. Called before run().
	 * @param context an instance of InvocationContext.
	 */
	public void setInvocationContext(InvocationContext context);

	/**
	 * The main work logic of the batch job. Will be called after setContext.
	 */
	public void run();

	/**
	 * @return one of the STATUS_* constants, or a value from 1-99 to indicate progress.
	 * Implementations need not support partial completion (progress) values, and can transition
	 * from STATUS_MIN_PROGRESS to STATUS_COMPLETE.
	 */
	public int getCompletionStatus();

	/**
	 * @return information about the batch job actions and results
	 */
	public InvocationResults getResults();

	/**
	 * @return a user-presentable note when an error occurs in batch processing. Will only
	 * be called if getCompletionStatus() returns STATUS_ERROR.
	 */
	public InvocationError getErrorInfo();

}
