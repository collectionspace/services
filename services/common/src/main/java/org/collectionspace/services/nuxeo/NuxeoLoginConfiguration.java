/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.collectionspace.services.nuxeo;

/**
 *
 * @author sanjaydalal
 */
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;

public class NuxeoLoginConfiguration extends Configuration {

    private final Configuration parent;
    public static final String LOGIN_DOMAIN = "nuxeo-client-login";

    public NuxeoLoginConfiguration(Configuration parent) {
        this.parent = parent;
    }

    @Override
    public AppConfigurationEntry[] getAppConfigurationEntry(String name) {

        if (LOGIN_DOMAIN.equals(name)) {
            AppConfigurationEntry[] entries = new AppConfigurationEntry[1];

            Map<String, Object> options = new HashMap<String, Object>();

            options.put("restore-login-identity", "True");
            options.put("multi-threaded", "True");

            entries[0] = new AppConfigurationEntry("org.jboss.security.ClientLoginModule", LoginModuleControlFlag.REQUIRED, options);


            return entries;
        } else {
            return parent.getAppConfigurationEntry(name);
        }
    }

    @Override
    public void refresh() {
        if (parent != null) {
            parent.refresh();
        }
    }
}
