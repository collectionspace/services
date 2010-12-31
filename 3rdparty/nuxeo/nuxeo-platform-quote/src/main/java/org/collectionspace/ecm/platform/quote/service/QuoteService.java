/*
 * (C) Copyright 2007 Nuxeo SAS (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */

package org.collectionspace.ecm.platform.quote.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import org.collectionspace.ecm.platform.quote.api.QuoteManager;
import org.collectionspace.ecm.platform.quote.impl.QuoteManagerImpl;

/**
 * @author <a href="mailto:glefter@nuxeo.com">George Lefter</a>
 *
 */
public class QuoteService extends DefaultComponent {

    public static final String ID = "org.collectionspace.ecm.platform.quote.service.QuoteService";

    public static final String VERSIONING_EXTENSION_POINT_RULES = "rules";

    private static final Log log = LogFactory.getLog(QuoteService.class);

    private QuoteManager quoteManager;

    private QuoteServiceConfig config;

    @Override
    public void registerContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        if ("config".equals(extensionPoint)) {
            config = (QuoteServiceConfig) contribution;
            log.debug("registered service config: " + config);
        } else {
            log.warn("unknown extension point: " + extensionPoint);
        }
    }

    @Override
    public void unregisterContribution(Object contribution,
            String extensionPoint, ComponentInstance contributor) {
        // do nothing
    }

    public QuoteManager getQuoteManager() {
        log.debug("getQuoteManager");
        if (quoteManager == null) {
            quoteManager = new QuoteManagerImpl(config);
        }
        return quoteManager;
    }

    public QuoteServiceConfig getConfig() {
        return config;
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == QuoteManager.class) {
            return adapter.cast(getQuoteManager());
        }
        return null;
    }

}
