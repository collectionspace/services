/**
 * This document is a part of the source code and related artifacts
 * for CollectionSpace, an open source collections management system
 * for museums and related institutions:
 *
 * http://www.collectionspace.org
 * http://wiki.collectionspace.org
 *
 * Copyright (c) 2009 Regents of the University of California
 *
 * Licensed under the Educational Community License (ECL), Version 2.0.
 * You may not use this file except in compliance with this License.
 *
 * You may obtain a copy of the ECL 2.0 License at
 * https://source.collectionspace.org/collection-space/LICENSE.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.collectionspace.services.client;

import javax.ws.rs.core.Response;

import org.apache.http.HttpStatus;
import org.collectionspace.services.description.ServiceDescription;
import org.collectionspace.services.jaxb.AbstractCommonList;

/**
 * A CollectionObjectClient.

 * @version $Revision:$
 */

public final class TestServiceClient extends AbstractServiceClientImpl<AbstractCommonList, Object, Object, TestServiceProxy> {

    public TestServiceClient() throws Exception {
		super();
	}

	/**
     *
     * Returning NULL since this class is a base-level client, used (only)
     * to obtain the base service URL.
     *
     */
    @Override
    public String getServicePathComponent() {
        throw new UnsupportedOperationException();
    }

	@Override
	public String getServiceName() {
		throw new UnsupportedOperationException();
    }

	@Override
	public Class<TestServiceProxy> getProxyClass() {
		// TODO Auto-generated method stub
		return TestServiceProxy.class;
	}

	@Override
	public Response create(Object payload) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Response update(String csid, Object payload) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Response read(String csid) {
        throw new UnsupportedOperationException();
	}

	@Override
	public Response readList() {
        throw new UnsupportedOperationException();
	}
	
	@Override
	public ServiceDescription getServiceDescription() {
		ServiceDescription result = null;
		
        Response res = getProxy().getServiceDescription();
        if (res.getStatus() == HttpStatus.SC_OK) {
        	result = (ServiceDescription) res.readEntity(ServiceDescription.class);
        }
        
        return result;
	}
}
